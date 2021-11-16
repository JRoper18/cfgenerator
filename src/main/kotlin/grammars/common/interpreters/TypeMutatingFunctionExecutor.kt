package grammars.common.interpreters

import grammar.ProductionRule
import grammar.StringSymbol
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.KeyedAttributesProductionRule

abstract class TypeMutatingFunctionExecutor(inTypes : List<String>,
                                            val outTypeMapper : SingleAttributeMapper,
                                            val childTypeIdxToMap : Int) :
    FunctionExecutor(inTypes) {
    override fun makeReturnTypeAPR(pr : ProductionRule, typeAttr : String): KeyedAttributesProductionRule {
        return AttributeMappingProductionRule(pr, typeAttr, childTypeIdxToMap, outTypeMapper)
    }
}