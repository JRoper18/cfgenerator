package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

class SizedListAttributeProductionRule(listName: NtSym,
                                       val unit: Symbol,
                                       separator: String = "") : KeyedAttributesProductionRule(

    listOf("length"),
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

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        if(attrs.size() > 1) {
            return cantMakeProgramReturn
        }
        if(attrs.size() == 0){
            return Pair(true, noConstraints)
        }
        val attr = attrs.toList()[0]
        val size = attr.second.toIntOrNull()
        val canMake = attr.first == "length" && (size != null) && size > 0
        if(!canMake) {
            return cantMakeProgramReturn
        }
        val constraints = if(!canMake || size == 0) noConstraints else listOf(
            listOf<RuleConstraint>(BasicRuleConstraint(Pair(attr.first, ((size ?: 1) - 1).toString()))),
            listOf(),
            listOf()
        )
        return Pair(canMake, constraints)
    }
}