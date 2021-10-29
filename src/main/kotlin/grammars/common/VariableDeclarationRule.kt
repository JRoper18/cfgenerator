package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

/**
 * Given a LHS symbol, a subrule that creates an attribute where the key is the name of the variable, and the name of that attribute,
 * Create a rule that creates variable declaration attributes.
 */
open class VariableDeclarationRule(lhs : Symbol, rhs : Symbol, subruleVarnameAttributeKey : String) :
    VariableAttributeRule(lhs, rhs, subruleVarnameAttributeKey, attrKeySuffix = "_is_decl") {
    override fun makeAttrValueFromChildren(childAttributes: NodeAttributes, attrKey: String): String {
        return "true"
    }

}