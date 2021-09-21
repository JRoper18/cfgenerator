package grammar.constraints

import grammar.NodeAttributes

data class IntRuleConstraint(val key : String, val expected : Int) : RuleConstraint{
    override fun satisfies(attrs: NodeAttributes): Boolean {
        return attrs.getIntAttribute(key) == expected
    }

}