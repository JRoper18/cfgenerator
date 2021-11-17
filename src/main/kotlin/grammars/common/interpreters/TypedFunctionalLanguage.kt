package grammars.common.interpreters

import grammar.*
import grammar.constraints.*
import grammars.CfgLanguage
import grammars.Language
import grammars.ProgramGenerationResult
import grammars.ProgramRunDetailedResult
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.*
import grammars.lambda2.Lambda2Grammar
import grammars.lambda2.Lambda2Interpreter
import utils.cartesian
import utils.combinations
import utils.combinationsTo
import utils.duplicates
import kotlin.random.Random

class TypedFunctionalLanguage(val basicTypesToValues : Map<String, Set<Any>>,
                              val complexTypes : Map<String, SingleAttributeMapper>,
                              val varName : StringsetSymbol,
                              val functions : Map<String, FunctionExecutor>,
                              val random : Random = Random,
                              val maxComplexTypeDepth : Int = 2,
) {


    val flattenedComplexTypes = complexTypes.entries.combinationsTo(maxComplexTypeDepth).cartesian(basicTypesToValues.keys).map {
        val startType = it.second
        var type = startType
        it.first.forEach { entry ->
            val mapper = entry.value
            type = mapper.forward(type)
        }
        val finalGenericType = it.first.last().key
        Pair(finalGenericType, type)
    }.groupBy {
        it.first
    }.map {
        Pair(it.key, it.value.map { it2 ->
            it2.second
        }.toSet())
    }.toMap()

    companion object {
        val typeAttr : String = "type"
        val constantAttr : String = "constant"
    }

    // Grammar stuff on the top

    protected val stmtSym : NtSym = NtSym("stmt")
    protected val varInit = NtSym("varInit")
    protected val declaredVar = NtSym("declared")
    protected val newVarRule = VariableDeclarationRule(varInit, varName, varName.attributeName)
    protected val typedNewVarRule = VariableChildValueRule(
        varInit,
        varName, newVarRule.subruleVarnameAttributeKey, "_type",
        typeAttr
    ).withOtherRule {
        SynthesizeAttributeProductionRule(mapOf(typeAttr to 0), it) // Bring up the ret type
    }
    protected val declaredRule = SynthesizeAttributeProductionRule(mapOf(varName.attributeName to 0, typeAttr to 0), (PR(
        declaredVar, listOf(varName))))

    protected val stmtIsDeclaredVarRule = SynthesizeAttributeProductionRule(
        mapOf(
            typeAttr to 0
        ), PR(stmtSym, listOf(declaredVar)))
    protected val lambdaArgs = NtSym("lambdaArgs")
    protected val lambdaArgsRule = OrderedListAttributeRule(ListProductionRule(listName = lambdaArgs, unitName = varInit, separator = ","),
        typeAttr, 3)
    protected val lambdaArgsInitRule = lambdaArgsRule.initListRule(0, PR(lambdaArgs, listOf(varInit)))
    protected val lambdaSym = NtSym("lambdaStmt")
    protected val lambdaRule = SynthesizeAttributeProductionRule(mapOf(typeAttr to 3) + lambdaArgsRule.attrKeysMade.map {
        Pair(it, 1) // Inherit every attribute from the lambdaArgs
    }.toMap(),
        PR(
            lambdaSym, listOf(
                StringSymbol("lambda"),
                lambdaArgs, COLON,
                stmtSym
            )),
    )
    protected val functionNamesToRules = functions.map {
        Pair(it.key, makeFunctionAPR(StringSymbol(it.key), it.value.numArgs) { pr ->
            it.value.makeReturnTypeAPR(this, pr, typeAttr)
        })
    }.toMap()

    protected val basicTypeConstantRules = basicTypesToValues.map {
        val sset = StringsetSymbol(it.value.map {
            it.toString()
        }.toSet(), attributeName = constantAttr)
        Pair(it.key, InitAttributeProductionRule(PR(stmtSym, listOf(sset)), typeAttr, it.key).withOtherRule { pr ->
            SynthesizeAttributeProductionRule(mapOf(sset.attributeName to 0), pr)
        })
    }.toMap()

    protected val strsToConstants = basicTypesToValues.values.flatten().map { constant ->
        Pair(constant.toString(), constant)
    }.toMap()

    protected val constantRules = basicTypeConstantRules.values.map {
        it.rule
    }.toSet()
    val grammar : AttributeGrammar = AttributeGrammar(
        givenRules = listOf(
            stmtIsDeclaredVarRule,
            newVarRule.withOtherRule(typedNewVarRule),
            declaredRule,
            lambdaRule,
            lambdaArgsRule,
            lambdaArgsInitRule,
            ) + basicTypeConstantRules.values + functionNamesToRules.values,
        constraints = mapOf(
                declaredRule.rule to VariableConstraintGenerator(varName.attributeName, newVarRule),
            ) + functions.map {
                Pair(functionNamesToRules[it.key]!!.rule, it.value.makeConstraints(
                    this,
                ))
            }.toMap(),
        start = lambdaSym,
        scopeCloserRules = setOf(
                lambdaRule.rule
        )
    )

    private fun makeFunctionPR(headerSymbol : StringSymbol, numArgs : Int) : ProductionRule {
        val pr = ProductionRule(stmtSym, listOf(headerSymbol, LP) + (0 until numArgs).flatMapIndexed { index, s ->
            if(index != 0){
                listOf(COMMA, stmtSym)
            }
            else {
                listOf(stmtSym)
            }
        } + listOf(RP))
        return pr
    }

    fun argIdxToChild(argIdx : Int) : Int {
        return (2 * argIdx) + 2
    }

    fun ithChildTypeKey(argIdx : Int) : String {
        return "${argIdx}.${typeAttr}"
    }

    private fun makeFunctionAPR(headerSymbol: StringSymbol, numArgs: Int, returnTypeAttrRuleMaker : (ProductionRule) -> (KeyedAttributesProductionRule)) : AttributedProductionRule{
        val pr = makeFunctionPR(headerSymbol = headerSymbol, numArgs = numArgs)
        val returnTypeAttrRule = returnTypeAttrRuleMaker(pr)
        require(returnTypeAttrRule.attrKeysMade == listOf(typeAttr)) {
            "Return type rule should only make an attribute with key $typeAttr but makes keys ${returnTypeAttrRule.attrKeysMade}"
        }
        // Map the 3rd entry to 0.type, 5th entry to 1.type, and so on.
        var apr : KeyedAttributesProductionRule = returnTypeAttrRule
        for (i in 0 until numArgs){
            val cidx = argIdxToChild(i)
            apr = apr.withOtherRule(KeyChangeAttributeRule(pr, typeAttr, cidx, ithChildTypeKey(i)))
        }
        return apr
    }
    // Then, the interpret stuff.
    class ParseError(msg : String) : IllegalStateException(msg)
    {
        constructor(msg : String, node : GenericGrammarNode, state : ProgramState) : this("Node:\n$node\n with program state: \n$state\n has error: \n$msg\n")
    }

    class TypeError(val wantedType : String) : IllegalArgumentException(
        "Wanted type $wantedType"
    )

    class InterpretError(msg : String) : IllegalStateException(msg)

    internal fun makeInput(type: String) : Any {
        if(type in basicTypesToValues.keys) {
            return basicTypesToValues[type]!!.random(this.random)
        }
        // Perhaps it's a complex type.
        for(cType in flattenedComplexTypes.keys) {
            val flattenedVal = flattenedComplexTypes[cType]!!
            if(type in flattenedVal) {
                if(cType == "list") {
                    // TODO avoid hardcoding this
                    val unwrappedType = complexTypes[cType]!!.backward(type)
                    require(unwrappedType.isNotEmpty()) {
                        "Unknown type $type!"
                    }
                    return List(IntRange(0, 5).random(this.random)) {
                        makeInput(unwrappedType[0])
                    }
                }
            }
        }
        throw IllegalArgumentException("Unknown type $type!")
    }
    // Returns a list of names and types of lambda arguments
    fun getLambdaData(node : GenericGrammarNode) : List<Pair<String, String>> {
        val lambdaArgsNode = node.rhs[1]
        val inputsNodeList : List<GenericGrammarNode>
        if(lambdaArgsNode.productionRule.rule  == lambdaArgsRule.rule) {
            inputsNodeList = (lambdaArgsNode.productionRule.rule as ListProductionRule).unroll(lambdaArgsNode).map {
                it.rhs[0]
            }
        }
        else {
            // It's a list initialization.
            inputsNodeList = listOf(lambdaArgsNode.rhs[0].rhs[0])
        }
        val inputTypeList = inputsNodeList.map {
            it.attributes().getStringAttribute(typeAttr)!!
        }
        val inputVarnames = inputsNodeList.map {
            it.attributes().getStringAttribute(varName.attributeName)!!
        }
        return inputVarnames.zip(inputTypeList)
    }
    fun makeExamples(progNode : RootGrammarNode, num : Int) : List<Pair<List<Any>, Any>> {
        val lambdaData = getLambdaData(progNode)
        var numFails = 0
        val goodExamples = mutableListOf<Pair<List<Any>, Any>>()
        val maxFailedExamples = 5
        for(i in 0 until num + maxFailedExamples) {
            val inputs = List<Any>(lambdaData.size) {
                makeInput(lambdaData[it].second)
            }
            var output : Any? = null
            try {
                println(inputs)
                output = interp(progNode, args = inputs)
            } catch (ex: Exception)  {
                when(ex) {
                    is InterpretError, is ParseError, is TypeError -> {
                        // Crap. Try again.
                        numFails += 1
                        if(numFails > maxFailedExamples) {
                            return goodExamples
                        }
                        continue // Don't hit the next part, where we solidify types and keep going.
                    }
                    else -> throw ex
                }
            }
            // If we're here the input worked.
            goodExamples.add(Pair(inputs, output))
            if(goodExamples.size >= num){
                return goodExamples;
            }
        }
        return goodExamples
    }

    fun interp(node : GenericGrammarNode, args : List<Any>, programState: ProgramState = ProgramState()) : Any {
        require(node.productionRule.rule == lambdaRule.rule) {
            "Must be a lambda rule: \n$node"
        }
        val stmtNode = node.rhs[3]
        // Update the program state.
        val lambdaData = getLambdaData(node)
        lambdaData.forEachIndexed { index, argPair ->
            val varname = argPair.first
            val vartype = argPair.second
            programState.setVar(varname, vartype, args[index])
        }
        val dupVarnames = lambdaData.map {
            it.first
        }.duplicates()
        if(dupVarnames.isNotEmpty()) {
            throw ParseError("Duplicate varnames $dupVarnames", node, programState)
        }
//        val numInputs = lambdaArgsNode.attributes().getStringAttribute("length")!!.toInt()
        val stmtResult = interpStmt(stmtNode, programState)
        lambdaData.forEach {
            programState.unsetVar(it.first)
        }
        return stmtResult
    }
    fun interpStmt(node : GenericGrammarNode, programState : ProgramState) : Any {
        var cidx = 2
        var argidx = 0
        if(node.productionRule.rule == stmtIsDeclaredVarRule.rule) {
            // It's a variable, just fetch the value of the variable.
            val varname = node.rhs[0].attributes().getStringAttribute(varName.attributeName)!!
            return programState.getVar(varname)!!
        }
        else if(constantRules.contains(node.productionRule.rule)){
            // It's a constant.
            val constantAttr = node.attributes().getStringAttribute(constantAttr)!!
            return strsToConstants[constantAttr]!!
        }

        val funcName = (node.rhs[0].productionRule.rule.lhs as StringSymbol).name
        val args = mutableListOf<Any>()
        while(cidx < node.rhs.size){
            val argTree = node.rhs[cidx]
            args.add(interpStmt(argTree, programState))
            argidx += 1
            cidx = argIdxToChild(argidx)
        }
        // Now we have all the args. Apply the function.
        val executor = functions[funcName]!!
        try {
            return executor.execute(args)
        } catch (ex : Exception) {
            throw InterpretError(ex.localizedMessage ?: ex.stackTraceToString())
        }
    }
}
