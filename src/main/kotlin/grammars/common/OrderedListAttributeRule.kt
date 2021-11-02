package grammars.common

import grammar.APR
import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.PR
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint
import java.util.Collections.max
import kotlin.math.max

class OrderedListAttributeRule(pr : ListProductionRule, val attrKey : String, maxDepth : Int = 10) : KeyedAttributesProductionRule(attrKeysMade = (0 until maxDepth).map {
    toAttrKey(it, attrKey)
}, pr) {
    companion object {
        fun fromAttrKey(totalKey : String, attrKey : String) : Int? {
            return totalKey.substringBefore(".${attrKey}").toIntOrNull()
        }
        fun toAttrKey(idx : Int, attrKey : String) : String {
            return "${idx}.${attrKey}"
        }
    }

    fun initListRule(childIdx : Int, rule : PR) : KeyedAttributesProductionRule {
        return KeyChangeAttributeRule(rule, attrKey, childIdx, toAttrKey(0, attrKey))
    }

    fun fromAttrKey(key : String) : Int? {
        return fromAttrKey(key, this.attrKey)
    }
    fun isOrderedListAttrKey(key : String) : Boolean {
        return fromAttrKey(key, attrKey) != null
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val lhsCons = mutableListOf<RuleConstraint>()
        var rhsAttr : NodeAttribute? = null
        var maxIdx = -1
        for(attr in attrs.toList()) {
            val neededIdx = fromAttrKey(attr.first, this.attrKey) ?: return cantMakeProgramReturn
            if(neededIdx > maxIdx) {
                // This is the highest so far, so it would be in the unit node.
                // Whatever is the current unit constraint turns into a list constraint.
                if(rhsAttr != null) {
                    lhsCons.add(BasicRuleConstraint(rhsAttr))
                }
                rhsAttr = attr
                maxIdx = neededIdx
            }
            else {
                // It must be on the LHS, because it's less than the max value we've seen.
                lhsCons.add(BasicRuleConstraint(NodeAttribute(toAttrKey(neededIdx, attr.first), attr.second)))
            }
        }
        if(rhsAttr == null) {
            // Never found a needed attr
            return Pair(true, noConstraints)
        }
        // Finally, unwrap the RHS attr.
        val finalRhsAttr = NodeAttribute(attrKey, rhsAttr.second)
        return Pair(true, listOf(lhsCons, listOf(), listOf(BasicRuleConstraint(finalRhsAttr))))
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
        return totalAttrs
    }


}