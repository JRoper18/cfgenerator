package grammars.common.interpreters

import grammar.ProductionRule
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.InitAttributeProductionRule
import grammars.common.rules.KeyChangeAttributeRule
import grammars.common.rules.KeyedAttributesProductionRule

abstract class HigherOrderFunctionExecutor(val lambdaArgTypes : List<String>, val totalOutTypeMapper : SingleAttributeMapper, otherArgs : List<String>) : FunctionExecutor(
    listOf("lambda") + otherArgs
) {

    protected fun lambdaArgAttrKey(argIdx : Int, typeAttr: String) : String {
        return "lambdaArgs.$argIdx.${typeAttr}"
    }

    override fun makeReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule,
    ): KeyedAttributesProductionRule {
        var apr : KeyedAttributesProductionRule = AttributeMappingProductionRule(pr, language.typeAttr, language.argIdxToChild(0), totalOutTypeMapper)
        lambdaArgTypes.forEachIndexed { index, argType ->
            val lambdaKey = language.ithLambdaArgTypeToKey(index)
            apr = apr.withOtherRule(KeyChangeAttributeRule(pr, lambdaKey, language.argIdxToChild(0), lambdaArgAttrKey(index, language.typeAttr)))
        }
        return apr
    }

}