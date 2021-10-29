package grammars.common

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.ProductionRule
import grammar.Symbol
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

abstract class VariableAttributeRule(lhs : Symbol, val rhs : Symbol, val subruleVarnameAttributeKey : String, val attrKeySuffix : String) :
    KeyedAttributesProductionRule(lowercaseASCII.map {
        it + attrKeySuffix
    }, ProductionRule(lhs, listOf(rhs))) {
    val attrKeyRegex = Regex("(\\w)$attrKeySuffix")
    abstract fun makeAttrValueFromChildren(childAttributes: NodeAttributes, attrKey : String) : String
    fun makeAttrKeyFromVarname(varname : String) : String {
        return "$varname$attrKeySuffix"
    }

    fun getVarnameFromAttrKey(key : String) : String {
        return attrKeyRegex.find(key)!!.destructured.component1()
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs : NodeAttributes = NodeAttributes()
        for(childAttrs in childAttributes){
            val varname = childAttrs.getStringAttribute(subruleVarnameAttributeKey) ?: continue // No varnames here
            val attrKey = makeAttrKeyFromVarname(varname)
            val attrVal = makeAttrValueFromChildren(childAttrs, attrKey)
            attrs.setAttribute(attrKey, attrVal)
        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val wantedAttrsFromSubrule = mutableListOf<NodeAttribute>()
        for(attr in attrs.toList()) {
            if(!attr.first.endsWith(attrKeySuffix)) {
                return Pair(false, listOf())
            }
            val varname = getVarnameFromAttrKey(attr.first)
            wantedAttrsFromSubrule.add(NodeAttribute(subruleVarnameAttributeKey, varname))
        }
        return Pair(true, listOf(wantedAttrsFromSubrule.map {
            BasicRuleConstraint(it)
        }))
    }
}