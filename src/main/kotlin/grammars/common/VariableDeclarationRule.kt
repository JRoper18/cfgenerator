package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

/**
 * Given a LHS symbol, a subrule that creates an attribute where the key is the name of the variable, and the name of that attribute,
 * Create a rule that creates variable declaration attributes.
 */
open class VariableDeclarationRule(lhs : Symbol, val rhs : Symbol, val subruleVarnameAttributeKey : String) :
    KeyedAttributesProductionRule(lowercaseASCII.map {
        DECLARED_VAR_ATTRIBUTE_KEY_REGEX_STR.replace("(\\w)", it)
    }, ProductionRule(lhs, listOf(rhs))) {
    companion object {
        const val DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX = "_is_decl"
        const val DECLARED_VAR_ATTRIBUTE_KEY_REGEX_STR = "(\\w)$DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX"
        val DECLARED_VAR_ATTRIBUTE_KEY_REGEX = Regex(DECLARED_VAR_ATTRIBUTE_KEY_REGEX_STR)
        fun makeAttrFromVarname(varname : String) : NodeAttribute {
            return NodeAttribute("$varname$DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX", "true")
        }

        fun getVarnameFromAttr(attr : NodeAttribute) : String {
            return DECLARED_VAR_ATTRIBUTE_KEY_REGEX.find(attr.first)!!.destructured.component1()

        }
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs : NodeAttributes = NodeAttributes()
        for(childAttrs in childAttributes){
            val varname = childAttrs.getStringAttribute(subruleVarnameAttributeKey) ?: continue // No varnames here
            val attr = makeAttrFromVarname(varname)
            attrs.setAttribute(attr.first, attr.second)
        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val wantedAttrsFromSubrule = mutableListOf<NodeAttribute>()
        for(attr in attrs.toList()) {
            if(!attr.first.endsWith(DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX)) {
                return Pair(false, listOf())
            }
            val varname = getVarnameFromAttr(attr)
            wantedAttrsFromSubrule.add(NodeAttribute(subruleVarnameAttributeKey, varname))
        }
        return Pair(true, listOf(wantedAttrsFromSubrule.map {
            BasicRuleConstraint(it)
        }))
    }
}