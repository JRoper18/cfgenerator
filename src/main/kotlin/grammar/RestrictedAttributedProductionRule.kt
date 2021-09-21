package grammar

import grammar.constraints.RuleConstraint

class RestrictedAttributedProductionRule(rule: ProductionRule, val synthesizedAttrs: List<String>, val inheritedAttrs: List<String>, constraints: List<RuleConstraint> = listOf()) : AttributedProductionRule(rule, constraints) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val newAttrs = NodeAttributes()
        childAttributes.forEach { attrs ->
            synthesizedAttrs.forEach {  attr ->
                attrs.copyAttribute(attr, newAttrs)
            }
        }
        return newAttrs
    }

    override fun makeInheritedAttributes(
        myIdx: Int,
        parentAttributes: NodeAttributes,
        siblingAttributes: List<NodeAttributes>
    ): NodeAttributes {
        val newAttrs = NodeAttributes()
        inheritedAttrs.forEach {  attr ->
            parentAttributes.copyAttribute(attr, newAttrs)
        }
        return newAttrs
    }

}