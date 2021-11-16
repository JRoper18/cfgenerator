package grammars.common.interpreters

import grammar.*
import grammar.constraints.*
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.*
import grammars.lambda2.Lambda2Grammar

class TypedFunctionalLanguage(val basicTypesToValues : Map<String, Set<String>>,
                              val complexTypes : Map<String, SingleAttributeMapper>,
                              val varName : StringsetSymbol,
                              val functions : Map<String, FunctionExecutor>
) {

    class ParseError(msg : String) : IllegalStateException(msg)
    {
        constructor(msg : String, node : GenericGrammarNode, state : ProgramState) : this("Node:\n$node\n with program state: \n$state\n has error: \n$msg\n")
    }

    class TypeError(val wantedType : String) : IllegalArgumentException(
        "Wanted type $wantedType"
    )

    class InterpretError(msg : String) : IllegalStateException(msg)

    val flattenedComplexTypes = complexTypes.map {
        Pair(it.key, basicTypesToValues.keys.map { bType ->
            it.value.forward(bType)
        })
    }.toMap()

    companion object {
        val typeAttr : String = "type"
    }
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
        Pair(it.key, InitAttributeProductionRule(PR(stmtSym, listOf(StringsetSymbol(it.value))), typeAttr, it.key))
    }.toMap()
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
                    this, basicTypesToValues.keys, flattenedComplexTypes
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

    fun interpStmt(node : GenericGrammarNode, programState : ProgramState) : Any {
        var cidx = 2
        var argidx = 0
        if(node.productionRule.rule == stmtIsDeclaredVarRule.rule) {
            // It's a variable, just fetch the value of the variable.
            val varname = node.rhs[0].attributes().getStringAttribute(varName.attributeName)!!
            return programState.getVar<Any>(varname)!!
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
        return functions[funcName]!!.execute(args)
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
}
