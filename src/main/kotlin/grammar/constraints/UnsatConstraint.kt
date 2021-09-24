package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import kotlin.random.Random

class UnsatConstraint : RuleConstraint {
    override fun satisfies(attrs: NodeAttributes): Boolean {
        return false
    }

    override fun makeSatisfyingAttribute(random : Random): NodeAttribute {
        return NodeAttribute("NULL_NEVER_GENERATE_THIS_ANYWHERE", "NULL_NEVER_GENERATE_THIS_ANYWHERE")
    }
}