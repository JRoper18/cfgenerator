package grammar.constraints

import grammar.NodeAttributes

interface ConstraintGenerator {
    fun generate(attrs: NodeAttributes) : List<RuleConstraint>
}