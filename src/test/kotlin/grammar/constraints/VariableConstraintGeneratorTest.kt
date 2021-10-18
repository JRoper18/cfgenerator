package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.StringsetSymbol
import grammars.common.VariableDeclarationRule
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class VariableConstraintGeneratorTest {

    @Test
    fun testGenerate() {
        val rule = StringsetSymbol(setOf("a", "b", "c"))
        val gen = VariableConstraintGenerator(rule.attributeName)
        val cons = gen.generate(NodeAttributes.fromAttr(VariableDeclarationRule.makeAttrFromVarname("a")))
        assertEquals(cons, listOf(BasicRuleConstraint(NodeAttribute(rule.attributeName, "a"))))
    }
}