package grammars.common.interpreters

import grammar.ProductionRule
import grammars.common.rules.InitAttributeProductionRule
import grammars.common.rules.KeyedAttributesProductionRule

abstract class BasicFunctionExecutor(inTypes : List<String>, val outType : String) : FunctionExecutor(inTypes) {
    override fun makeReturnTypeAPR(pr: ProductionRule, typeAttr: String): KeyedAttributesProductionRule {
        return InitAttributeProductionRule(pr, typeAttr, outType)
    }
}