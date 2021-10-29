package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammars.common.VariableAttributeRule
import grammars.common.VariableDeclarationRule
import kotlin.random.Random

/**
 * Given the attribute key name that signifies that the attribute value is a variable name,
 * Create constraints that say "the varnameAttribute has to have a value that is an existing variable".
 */
class VariableConstraintGenerator(val varnameAttrKey: String, val variableAttributeRule: VariableAttributeRule) : ConstraintGenerator {
    override fun generate(attrs: NodeAttributes, random: Random): List<RuleConstraint> {
        val matchingKeys = attrs.matchingAttributeKeys(variableAttributeRule.attrKeyRegex)
        if(matchingKeys.isEmpty()) {
            return listOf(UnsatConstraint())
        }
        val varnames = matchingKeys.map {
            variableAttributeRule.getVarnameFromAttrKey(it)
        }
        return listOf(BasicRuleConstraint(NodeAttribute(varnameAttrKey, varnames.random(random))))
    }
}