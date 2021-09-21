package grammar.constraints

import grammar.NodeAttributes

sealed interface RuleConstraint {
    fun satisfies(attrs : NodeAttributes) : Boolean
}
