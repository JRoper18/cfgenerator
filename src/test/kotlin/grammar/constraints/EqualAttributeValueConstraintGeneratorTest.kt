package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class EqualAttributeValueConstraintGeneratorTest {

    @Test
    fun testGenerate() {
        val attr1 = NodeAttribute("a", "1")
        val attr2 = NodeAttribute("b", "2")
        val gen = EqualAttributeValueConstraintGenerator(setOf("a", "b"), possibleValues = setOf())
        val made1 = gen.generate(NodeAttributes.fromList(listOf(attr1, attr2)))
        assertEquals(listOf(BasicRuleConstraint(attr1), BasicRuleConstraint(NodeAttribute("b", "1"))), made1)
    }

    @Test
    fun testGenerateWithNoExisting() {
        val gen = EqualAttributeValueConstraintGenerator(setOf("a", "b"), possibleValues = setOf("1"))
        val made1 = gen.generate(NodeAttributes())
        assertEquals(made1, listOf(BasicRuleConstraint(NodeAttribute("a", "1")), BasicRuleConstraint(NodeAttribute("b", "1"))))
    }
}