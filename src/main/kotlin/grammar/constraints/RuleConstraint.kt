package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import kotlin.random.Random

sealed interface RuleConstraint {
    fun satisfies(attrs : NodeAttributes) : Boolean
    fun makeSatisfyingAttribute(random: Random = Random(42L)) : NodeAttribute
}
