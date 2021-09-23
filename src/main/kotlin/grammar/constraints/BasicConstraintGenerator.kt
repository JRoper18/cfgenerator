package grammar.constraints

import grammar.NodeAttributes

class BasicConstraintGenerator(val constraints: List<RuleConstraint>) : ConstraintGenerator {
    override fun generate(attrs: NodeAttributes): List<RuleConstraint> {
        return constraints
    }
}