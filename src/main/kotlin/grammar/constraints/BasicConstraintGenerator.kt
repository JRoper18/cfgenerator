package grammar.constraints

import grammar.NodeAttributes
import kotlin.random.Random

class BasicConstraintGenerator(val constraints: List<RuleConstraint>) : ConstraintGenerator {
    override fun generate(attrs: NodeAttributes, random: Random): List<RuleConstraint> {
        return constraints
    }
}