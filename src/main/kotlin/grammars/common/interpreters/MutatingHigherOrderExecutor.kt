package grammars.common.interpreters

import grammar.ProductionRule
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.KeyedAttributesProductionRule

abstract class MutatingHigherOrderExecutor(lambdaArgTypes : List<String>, otherArgs : List<String>,
                                           val argIdx : Int,
                                           val outTypeMapper : SingleAttributeMapper,
) : HigherOrderFunctionExecutor(lambdaArgTypes, otherArgs) {
    override fun makeLambdaReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        return AttributeMappingProductionRule(pr, language.typeAttr, language.argIdxToChild(argIdx), outTypeMapper)
    }
}