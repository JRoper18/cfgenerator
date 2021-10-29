package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

/**
 * Given a mapping from strings to uplift to which children to uplift them from, copy attributes from children.
 */
class SynthesizeAttributeProductionRule(val toSynthesize: Map<String, Int>, rule: ProductionRule) : KeyedAttributesProductionRule(toSynthesize.keys.toList(), rule) {

    val synthesizedKeys : Set<String> by lazy {
        toSynthesize.keys
    }

    init {
        toSynthesize.forEach {
            require(it.value < rule.rhs.size && it.value >= 0) {
                "The index of the child to synthesize from must be within the bounds of the rule's RHS  "
            }
        }
    }
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs = NodeAttributes()
        toSynthesize.forEach { key, childIdx ->
            childAttributes[childIdx].copyAttributeIfPresent(key, attrs)
        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val constraintList = rule.rhs.map {
            mutableListOf<RuleConstraint>()
        }.toMutableList()
        attrs.toList().forEachIndexed { index, attr ->
            if(toSynthesize.containsKey(attr.first)){
                val childIdx = toSynthesize.get(attr.first)!!
                constraintList[childIdx].add(BasicRuleConstraint(attr))
            }
            else {
                return cantMakeProgramReturn // We can't make that attribute.
            }
        }
        return Pair(true, constraintList.map {
            it.toList()
        }.toList())
    }

}