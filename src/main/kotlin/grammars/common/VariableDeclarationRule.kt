package grammars.common

import grammar.*
import grammar.constraints.RuleConstraint

/**
 * Given a LHS symbol, a subrule that creates an attribute where the key is the name of the variable, and the name of that attribute,
 * Create a rule that creates variable declaration attributes.
 */
class VariableDeclarationRule(val lhs : Symbol, val varnameSubrule : AttributedProductionRule, val subruleVarnameAttributeKey : String) :
    AttributedProductionRule(ProductionRule(lhs, listOf(varnameSubrule.rule.lhs))) {
    companion object {
        const val DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX = "_is_declared_var"
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs : NodeAttributes = NodeAttributes()
        childAttributes.forEach {
            val varname = it.getStringAttribute(subruleVarnameAttributeKey)
            attrs.setAttribute("$varname$DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX", "true")
        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val wantedAttrsFromSubrule = mutableListOf<NodeAttribute>()
        for(attr in attrs.toList()) {
            if(!attr.first.endsWith(DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX)) {
                return Pair(false, listOf())
            }
            val varname = attr.first.removeSuffix(DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX)
            wantedAttrsFromSubrule.add(NodeAttribute(subruleVarnameAttributeKey, varname))
        }
        return varnameSubrule.canMakeProgramWithAttributes(NodeAttributes.fromList(wantedAttrsFromSubrule))
    }

    override fun makeChildrenForAttributes(
        attrs: NodeAttributes,
        nodesThatFit: List<GenericGrammarNode>
    ): List<GenericGrammarNode> {
        return listOf(RootGrammarNode(this.varnameSubrule).withChildren(this.varnameSubrule.makeChildrenForAttributes(attrs, nodesThatFit)))
    }
}