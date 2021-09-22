package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes

sealed interface RuleConstraint {
    fun satisfies(attrs : NodeAttributes) : Boolean
    fun makeSatisfyingAttribute() : NodeAttribute
}
