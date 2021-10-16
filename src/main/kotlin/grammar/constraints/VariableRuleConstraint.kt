package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammars.common.VariableDeclarationRule
import kotlin.random.Random

/**
 * If this constraint is on a node, it means that the varnameAttributeKey needs to be equal to a variable that exists.
 */
data class VariableRuleConstraint(val varnameAttributeKey : String) : RuleConstraint {
    companion object {
        const val IS_VARNAME_REGEX = "[a-z]" // Make this more complex later.
    }
    override fun satisfies(attrs: NodeAttributes): Boolean {
        val neededVarName = attrs.getStringAttribute(varnameAttributeKey) ?: return false // If there's no variable attribute, false by default.
        val matchingKeys = attrs.matchingAttributeKeys(Regex("$IS_VARNAME_REGEX${VariableDeclarationRule.DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX}"))
        // Return true if there's a matching key, and the attribute that says if it's declared is true.
        return matchingKeys.isNotEmpty() && matchingKeys.filter {
            attrs.getStringAttribute(it) == "true"
        }.size == matchingKeys.size
    }

    override fun makeSatisfyingAttribute(random: Random): NodeAttribute {
        // Return a random string that fits the VARNAME_REGEX and then append the declared vars suffix.
        val attrKey = ('a'..'z').random(random)
        return NodeAttribute(attrKey.toString(), "true")
    }
}