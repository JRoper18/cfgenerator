package grammars.common

import grammar.AttributedProductionRule
import grammar.ProductionRule

/**
 * This is a class that defines the attribute keys it will generate.
 * The benefit of this is that if you know which attribute keys you're going to make,
 * You can "merge" this attribute rule with other rules, provided you don't have overlap in the keys you make.
 */
open class KeyedAttributesProductionRule(val attrKeysMade : List<String>, rule : ProductionRule) : AttributedProductionRule(rule) {
    fun withOtherRule(otherRule: KeyedAttributesProductionRule) : KeyedAttributesProductionRule {
        return CombinedKeyedAttributesRule(listOf(this, otherRule))
    }

    fun withOtherRule(makeOtherRule : (pr : ProductionRule) -> KeyedAttributesProductionRule) : KeyedAttributesProductionRule {
        return withOtherRule(makeOtherRule(this.rule))
    }
}