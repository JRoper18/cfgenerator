package grammar.constraints

import grammar.NodeAttributes
import kotlin.random.Random

interface ConstraintGenerator {
    fun generate(attrs: NodeAttributes, random: Random = Random(42L)) : List<RuleConstraint>
}