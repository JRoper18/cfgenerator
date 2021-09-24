package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class LookupConstraintGeneratorTest {

    @Test
    fun testGenerateValid() {
        val attrs = NodeAttributes()
        attrs.setAttribute("chosenSymbol", "Head")
        val gen = LookupConstraintGenerator("chosenSymbol", "length", mapOf(
            "Head" to "2",
            "NotHead" to "3"
        ))
        val gen1 = gen.generate(attrs)
        assertEquals(gen1[0], BasicRuleConstraint(NodeAttribute("length", "2")))

        attrs.setAttribute("chosenSymbol", "NotHead")
        val gen2 = gen.generate(attrs)
        assertEquals(gen2[0], BasicRuleConstraint(NodeAttribute("length", "3")))
    }
    @Test
    fun testGenerateInvalid() {
        val attrs = NodeAttributes()
        attrs.setAttribute("chosenSymbol", "NotInMap")
        val gen = LookupConstraintGenerator("chosenSymbol", "length", mapOf(
            "Head" to "2",
            "NotHead" to "3"
        ))
        val gen1 = gen.generate(attrs)
        assertEquals(gen1[0].javaClass, UnsatConstraint::class.java)

    }


}