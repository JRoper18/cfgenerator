package grammars.common.rules

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.PR
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint
import kotlin.math.max

class OrderedListAttributeRule(pr : ListProductionRule, val attrKey : String, val listLength : Int = 10) :
KeyedAttributesProductionRule(attrKeysMade = (0 until (listLength)).map {
    toAttrKey(it, attrKey)
} + SizedListAttributeProductionRule(pr).attrKeysMade, pr) {
    val slapr = SizedListAttributeProductionRule(pr)
    companion object {
        fun fromAttrKey(totalKey : String, attrKey : String) : Int? {
            return totalKey.substringBefore(".${attrKey}").toIntOrNull()
        }
        fun toAttrKey(idx : Int, attrKey : String) : String {
            return "${idx}.${attrKey}"
        }
    }
    init {
        require(listLength >= 2) {
            "List length must be >= 2"
        }
    }

    fun initListRule(childIdx : Int, rule : PR) : KeyedAttributesProductionRule {
        require(rule.lhs == this.rule.lhs) {
            "LHS of init rule and ordered list rule must match"
        }
        return KeyChangeAttributeRule(rule, attrKey, childIdx, toAttrKey(0, attrKey)).withOtherRule {
            InitAttributeProductionRule(rule, "length", "1")
        }
    }

    fun fromAttrKey(key : String) : Int? {
        return fromAttrKey(key, this.attrKey)
    }
    fun isOrderedListAttrKey(key : String) : Boolean {
        return fromAttrKey(key, attrKey) != null
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val lhsCons = mutableListOf<RuleConstraint>()
        var rhsAttr = mutableListOf<RuleConstraint>()
        val givenLength = attrs.getStringAttribute("length")?.toInt()
        // Let the sized list handle ths.
        if(givenLength != null) {
            val canMakeLength = slapr.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("length", givenLength.toString())))
            if(!canMakeLength.first) {
                return cantMakeProgramReturn
            }
            lhsCons.addAll(canMakeLength.second[0])
        }
        val boundedLength = givenLength ?: listLength
        if(boundedLength <= 1 || (boundedLength > listLength)) {
            // The LHS is 1, plus the RHS is 1, so the min length is two.
            // Or, we're asking for attributes past the bounded length.
            return cantMakeProgramReturn
        }
        // If we have a bounded length, we should've found it by now.
        for(attr in attrs.toList()){
            if(attr.first == "length") {
                continue
            }
            val neededIdx = fromAttrKey(attr.first, this.attrKey) ?: return cantMakeProgramReturn
            if(neededIdx >= boundedLength) {
                // Can't make this: The length is too small.
                return cantMakeProgramReturn
            }
            else if(neededIdx == boundedLength - 1){
                // This one is on the RHS, because it's the last in the lst.
                rhsAttr.add(BasicRuleConstraint(NodeAttribute(attrKey, attr.second)))
            }
            else {
                // It must be on the LHS, because it's less than the max value we've seen.
                lhsCons.add(BasicRuleConstraint(attr))
            }
        }
        return Pair(true, listOf(lhsCons, listOf(), rhsAttr))
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val listAttrs = childAttributes[0]
        val unitAttrs = childAttributes[2]
        val totalAttrs = NodeAttributes()
        var maxIdx = -1
        for(attr in listAttrs.toList()) {
            val attrIdx = fromAttrKey(attr.first)
            if(attrIdx != null) {
                listAttrs.copyAttribute(attr.first, totalAttrs)
                maxIdx = max(attrIdx, maxIdx)
            }
        }
        val newAttrVal = unitAttrs.getStringAttribute(attrKey)!!
        totalAttrs.setAttribute(toAttrKey(maxIdx + 1, attrKey), newAttrVal)
        return totalAttrs.union(this.slapr.makeSynthesizedAttributes(childAttributes))
    }


}