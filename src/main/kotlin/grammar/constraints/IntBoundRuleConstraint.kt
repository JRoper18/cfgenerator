package grammar.constraints

import grammar.NodeAttributes

data class IntBoundRuleConstraint(val key: String, val minimum: Int? = null, val maximum: Int? = null) : RuleConstraint {
    override fun satisfies(attrs: NodeAttributes): Boolean {
        val value = attrs.getIntAttribute(key) ?: return false
        return (minimum == null || value >= minimum) && (maximum == null || value < maximum)
    }
    


}