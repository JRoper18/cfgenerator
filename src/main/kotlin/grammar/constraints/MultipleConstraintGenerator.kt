package grammar.constraints

import grammar.NodeAttributes
import kotlin.random.Random

class MultipleConstraintGenerator(val generators : List<ConstraintGenerator>) : ConstraintGenerator {
    override fun generate(attrs: NodeAttributes, random: Random): List<RuleConstraint> {
        return generators.map {
            it.generate(attrs, random)
        }.flatten()
    }

    override fun and(other: ConstraintGenerator): ConstraintGenerator {
        return MultipleConstraintGenerator(this.generators + other)
    }
}