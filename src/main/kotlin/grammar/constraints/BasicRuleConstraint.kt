package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes

data class BasicRuleConstraint(val attr: NodeAttribute) : RuleConstraint {
    override fun satisfies(attrs: NodeAttributes): Boolean {
        return attrs.getStringAttribute(attr.first).equals(attr.second)
    }

    override fun makeSatisfyingAttribute(): NodeAttribute {
        return attr
    }
}