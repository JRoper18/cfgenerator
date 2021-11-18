package grammars.common.interpreters

import generators.ProgramStringifier
import grammar.*
import grammar.constraints.*
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.*
import utils.cartesian
import utils.combinationsTo
import utils.duplicates
import kotlin.random.Random

abstract class TypedFunctionalLanguage(val basicTypesToValues : Map<String, Set<Any>>,
                              val complexTypes : Map<String, SingleAttributeMapper>,
                              val varName : StringsetSymbol,
                              val functions : Map<String, FunctionExecutor>,
                              val typeAttr : String,
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


    val constantAttr : String = "constant"
    val lambdaType : String = "lambda"
    val lambdaRetType : String = "retType"
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
    protected val lambdaRule = SynthesizeAttributeProductionRule(
        mapOf(typeAttr to 3) + lambdaArgsRule.attrKeysMade.map {
        Pair(it, 1) // Inherit every attribute from the lambdaArgs
    }.toMap(),
        PR(
            lambdaSym, listOf(
                StringSymbol(lambdaType),
                lambdaArgs, COLON,
                stmtSym
            )),
    )
    protected val functionNamesToRules = functions.map {
        val lambdaIdx : Int?
        if(it.value is HigherOrderFunctionExecutor) {
            lambdaIdx = 0
        }
        else {
            lambdaIdx = null
        }
        Pair(it.key, makeFunctionAPR(StringSymbol(it.key), it.value.numArgs, lambdaIdx) { pr ->
            it.value.makeReturnTypeAPR(this, pr)
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

    abstract fun makeFunctionPR(headerSymbol : StringSymbol, numArgs : Int, lambdaIdx : Int? = null) : ProductionRule

    abstract fun argIdxToChild(argIdx : Int) : Int

    abstract fun ithChildTypeKey(argIdx : Int) : String

    abstract fun ithLambdaArgTypeToKey(argIdx: Int) : String

    private fun makeFunctionAPR(headerSymbol: StringSymbol, numArgs: Int, lambdaIdx : Int?, returnTypeAttrRuleMaker : (ProductionRule) -> (KeyedAttributesProductionRule)) : AttributedProductionRule{
        val pr = makeFunctionPR(headerSymbol = headerSymbol, numArgs = numArgs, lambdaIdx=lambdaIdx)
        val returnTypeAttrRule = returnTypeAttrRuleMaker(pr)
        require(typeAttr in returnTypeAttrRule.attrKeysMade) {
            "Return type rule should make an attribute with key $typeAttr but makes keys ${returnTypeAttrRule.attrKeysMade}"
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

        constructor(msg : String, tokens : List<String>, state : ProgramState) : this("At program: ${tokens.joinToString(" ")} and state \n$state\n parse error with msg \n$msg\n")
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
                output = interp(stringifier.stringify(progNode), args = argsToStr(inputs))
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

    val stringifier = ProgramStringifier(" ")
    fun areTokensLambda(tokens : List<String>) : Boolean {
        return tokens[0] == lambdaType
    }
    abstract fun lambdaVarnames(tokens : List<String>) : List<String>
    abstract fun getStmtFromLambda(tokens : List<String>) : List<String>
    /**
     * Returns the function names and the tokens for each of it's args.
     */
    abstract fun extractStmtData(tokens : List<String>) : Pair<String, List<List<String>>>

    abstract fun argsFromStr(args : String) : List<Any>
    abstract fun argsToStr(args : List<Any>) : String

    fun interp(progStr : String, args : String) : Any {
        return interpTokens(progStr.split(" ").map {
           it.trim()
        }.filter { it.isNotBlank() }, argsFromStr(args))
    }
    fun interpTokens(tokens : List<String>, args : List<Any>, programState : ProgramState = ProgramState()) : Any {
        // Our language has two super-simple types of expressions: Statements and lambdas.
        if(areTokensLambda(tokens)) {
            val varnames = lambdaVarnames(tokens)
            if(varnames.size != args.size) {
                throw ParseError("Args $args don't match lambda $tokens")
            }
            val dupVarnames = varnames.duplicates()
            if(dupVarnames.isNotEmpty()) {
                throw ParseError("Duplicate varnames $dupVarnames", tokens, programState)
            }
            varnames.forEachIndexed { index, name ->
                // Type is any if we don't know.
                programState.setVar(name, "any", args[index])
            }
            val internalStmt = getStmtFromLambda(tokens)
            val stmtResult = interpTokens(internalStmt, listOf(), programState = programState)
            varnames.forEach {
                programState.unsetVar(it)
            }
            return stmtResult
        }
        else {
            // It's a statement. Either a variable, a constant, or a function call.
            if(tokens.size == 1) {
                // Variable, or constant?
                return strsToConstants[tokens[0]] ?: programState.getVar(tokens[0]) ?: throw ParseError("Unknown token ${tokens[0]}")
            }
            // Else, it's a function call.
            val stmtData = extractStmtData(tokens)
            val funcName = stmtData.first
            val executor = functions[funcName] ?: throw ParseError("Unknown function name $funcName")

            val interpedArgs = stmtData.second.mapIndexed { index, argTokens ->
                if((index == 0) && executor is HigherOrderFunctionExecutor) {
                    // Lambda index at zero, just return the tokens unmodified.
                    argTokens
                }
                else {
                    interpTokens(argTokens, listOf(), programState = programState)
                }
            }
            try {
                return executor.execute({ prog, theirArgs -> this.interpTokens(prog as List<String>, theirArgs, programState) }, interpedArgs)
            } catch (ex : Exception) {
                throw InterpretError(ex.stackTraceToString())
            }
        }
    }
}
