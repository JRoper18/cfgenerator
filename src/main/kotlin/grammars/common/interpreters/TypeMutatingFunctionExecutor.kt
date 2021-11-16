package grammars.common.interpreters

import grammar.ProductionRule
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.KeyedAttributesProductionRule

/**
 * When determining return type, it's a mutation/map from an initial type of the child at the index.
 */
abstract class TypeMutatingFunctionExecutor(inTypes : List<String>,
                                            val outTypeMapper : SingleAttributeMapper,
                                            val argTypeIdx : Int) :
    FunctionExecutor(inTypes) {
    override fun makeReturnTypeAPR(language: TypedFunctionalLanguage, pr : ProductionRule, typeAttr : String, ): KeyedAttributesProductionRule {
        return AttributeMappingProductionRule(pr, typeAttr, language.argIdxToChild(argTypeIdx), outTypeMapper)
    }
}