package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OrRuleConstraintTest {

    val attrs = listOf(NodeAttribute("a", "1"), NodeAttribute("b", "2"), NodeAttribute("c", "3"))
    val cons = OrRuleConstraint(attrs.map {
        BasicRuleConstraint(it)
    })

    @Test
    fun testSatisfies() {
        attrs.forEach {
            assert(cons.satisfies(NodeAttributes.fromAttr(it)))
        }
    }

    @Test
    fun testMakeSatisfyingAttribute() {
        repeat(10) {
            assert(cons.makeSatisfyingAttribute() in attrs)
        }
    }
}