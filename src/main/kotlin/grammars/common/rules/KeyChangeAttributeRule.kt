package grammars.common.rules

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.ProductionRule
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

/**
 * Just changes a single key, keeps the value.
 */
class KeyChangeAttributeRule(rule : ProductionRule, val initialAttrKey : String, val childIdx : Int, val newKey : String): KeyedAttributesProductionRule(
    listOf(newKey), rule
) {
    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        if(attrs.size() != 1) {
            return cantMakeProgramReturn
        }
        val attr = attrs.toList()[0]
        if(attr.first != newKey) {
            return cantMakeProgramReturn
        }
        return Pair(true, rule.rhs.mapIndexed { index, symbol ->
            if(index == childIdx) {
                listOf(BasicRuleConstraint(NodeAttribute(initialAttrKey, attr.second)))
            }
            else {
                listOf()
            }
        })
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val initialVal = childAttributes[childIdx].getStringAttribute(initialAttrKey) ?: return NodeAttributes()
        return NodeAttributes.fromAttr(
            NodeAttribute(newKey, initialVal)
        )
    }
}