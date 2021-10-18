package grammars.common

import grammar.*
import grammar.constraints.RuleConstraint

class InitAttributeProductionRule(rule: ProductionRule, val initialKey : String, val initialVal : String) : SingleAttributeProductionRule(rule) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val ret = NodeAttributes()
        ret.setAttribute(initialKey, initialVal)
        return ret
    }
    override fun canMakeProgramWithAttribute(attr: NodeAttribute) : Pair<Boolean, List<List<RuleConstraint>>> {
        return Pair(attr.first == initialKey && attr.second == initialVal, noConstraints)
    }

    /**
     * Assume someone's program has the rules needed. Here, we assemble a program with the given nodes.
     * If we return a list of constraints, the input is a list of programs that satisfy the constrains in the order we return them.
     */
    override fun makeChildrenForAttribute(
        attr: NodeAttribute,
        nodesThatFit: List<GenericGrammarNode>
    ): List<GenericGrammarNode> {
        return this.rule.rhs.map { symbol ->
            RootGrammarNode(UnexpandedAPR(symbol))
        }
    }
}