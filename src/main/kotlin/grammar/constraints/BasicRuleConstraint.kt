package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import kotlin.random.Random

data class BasicRuleConstraint(val attr: NodeAttribute) : RuleConstraint {
    override fun satisfies(attrs: NodeAttributes): Boolean {
        return (attrs.getStringAttribute(attr.first) ?: return false) == (attr.second)
    }

    override fun makeSatisfyingAttribute(random : Random): NodeAttribute {
        return attr
    }
}