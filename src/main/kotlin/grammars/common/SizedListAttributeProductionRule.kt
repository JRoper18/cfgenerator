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
        val childLength = listAttrs.getStringAttribute(sizeKey)
        if(childLength != null){
            ret.setAttribute(sizeKey, (listAttrs.getStringAttribute(sizeKey)!!.toInt() + 1).toString())
        }
        return ret
    }

    override fun canMakeProgramWithAttribute(attr: NodeAttribute): Pair<Boolean, List<RuleConstraint>> {
        val size = attr.second.toIntOrNull()
        val canMake = attr.first == "length" && (size != null) && size > 0
        val constraints = if(!canMake || size == 0) listOf() else listOf<RuleConstraint>(BasicRuleConstraint(Pair(attr.first, ((size ?: 1) - 1).toString())))
        return Pair(canMake, constraints)
    }

    override fun makeChildrenForAttribute(
        attr: NodeAttribute,
        nodeThatFits: GenericGrammarNode?
    ): List<GenericGrammarNode> {
        return listOf(
            nodeThatFits!!,
            RootGrammarNode(TerminalAPR(this.rule.rhs[1])),
            RootGrammarNode(UnexpandedAPR(this.rule.rhs[2])),
        )
    }
}