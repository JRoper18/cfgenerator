package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

class SizedListAttributeProductionRule(listName: NtSym,
                                       unitName: Symbol,
                                       separator: String = "") : AttributedProductionRule(
    ListProductionRule(listName, unitName, separator)) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val listAttrs = childAttributes[0]
        val sizeKey = "length"
        val ret = listAttrs.copy()
        ret.setAttribute(sizeKey, (listAttrs.getStringAttribute(sizeKey)!!.toInt() + 1).toString())
        return ret
    }

    override fun canMakeProgramWithAttribute(attr: NodeAttribute): Pair<Boolean, List<RuleConstraint>> {
        val size = attr.second.toIntOrNull()
        val canMake = attr.first == "length" && (size != null) && size >= 0
        val constraints = if(!canMake || size == 0) listOf() else listOf<RuleConstraint>(BasicRuleConstraint(Pair(attr.first, ((size ?: 1) - 1).toString())))
        return Pair(canMake, constraints)
    }

    override fun makeProgramWithAttribute(attr: NodeAttribute, node: GenericGrammarNode?): GenericGrammarNode {
        // Node here must be a node that satisfies the constraint of being length - 1.
        val ret = RootGrammarNode(this)
        ret.rhs = listOf(
            node!!.withParent(ret, 0),
            GrammarNode(TerminalAPR(this.rule.rhs[1]), ret, 1),
            GrammarNode(UnexpandedAPR(this.rule.rhs[2]), ret, 2),
        )
        return ret

    }
}