package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

/**
 * Given a mapping from strings to uplift to which children to uplift them from, copy attributes from children.
 */
class SynthesizeAttributeProductionRule(val toSynthesize: Map<String, Int>, rule: ProductionRule) : AttributedProductionRule(rule) {

    init {
        toSynthesize.forEach {
            require(it.value < rule.rhs.size && it.value >= 0) {
                "The size of the rule's RHS must match the size of the synthesis list. "
            }
        }
    }
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs = NodeAttributes()
        toSynthesize.forEach { key, childIdx ->
            childAttributes[childIdx].copyAttribute(key, attrs)
        }
        return attrs
    }

    override fun canMakeProgramWithAttribute(attr: NodeAttribute): Pair<Boolean, List<RuleConstraint>> {
        if(toSynthesize.containsKey(attr.first)){
            return Pair(true, listOf(BasicRuleConstraint(attr)))
        }
        return Pair(false, listOf())
    }

    override fun makeChildrenForAttribute(
        attr: NodeAttribute,
        nodeThatFits: GenericGrammarNode?
    ): List<GenericGrammarNode> {
        val idxToInsert = toSynthesize[attr.first]
        return this.rule.rhs.mapIndexed { index, symbol ->
            var ret = nodeThatFits!!
            if(index != idxToInsert){
                ret = RootGrammarNode(UnexpandedAPR(symbol))
            }
            ret
        }
    }
}