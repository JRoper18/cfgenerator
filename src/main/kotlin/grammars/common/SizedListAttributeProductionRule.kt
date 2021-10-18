package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

class SizedListAttributeProductionRule(listName: NtSym,
                                       val unit: Symbol,
                                       separator: String = "") : SingleAttributeProductionRule(


    ListProductionRule(listName, unit, separator)) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val listAttrs = childAttributes[0]
        val sizeKey = "length"
        val ret = listAttrs.copy()
        val childLength = listAttrs.getStringAttribute(sizeKey)
        if(childLength != null){
            ret.setAttribute(sizeKey, (listAttrs.getStringAttribute(sizeKey)!!.toInt() + 1).toString())
        }
        return ret
    }

    override fun canMakeProgramWithAttribute(attr: NodeAttribute): Pair<Boolean, List<List<RuleConstraint>>> {
        val size = attr.second.toIntOrNull()
        val canMake = attr.first == "length" && (size != null) && size > 0
        val constraints = if(!canMake || size == 0) listOf() else listOf(listOf<RuleConstraint>(BasicRuleConstraint(Pair(attr.first, ((size ?: 1) - 1).toString()))))
        return Pair(canMake, constraints)
    }

    override fun makeChildrenForAttribute(
        attr: NodeAttribute,
        nodesThatFit: List<GenericGrammarNode>
    ): List<GenericGrammarNode> {
        val unitRule = if(this.unit.terminal) TerminalAPR(this.rule.rhs[2]) else UnexpandedAPR(this.rule.rhs[2])
        return listOf(
            nodesThatFit[0],
            RootGrammarNode(TerminalAPR(this.rule.rhs[1])),
            RootGrammarNode(unitRule),
        )
    }


}