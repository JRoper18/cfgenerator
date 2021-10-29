package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.NtSym
import grammar.StringsetSymbol
import grammars.common.VariableDeclarationRule
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class VariableConstraintGeneratorTest {

    @Test
    fun testGenerate() {
        val rhs = StringsetSymbol(setOf("a", "b", "c"))
        val rule = VariableDeclarationRule(NtSym("lhs"), rhs, rhs.attributeName)
        val gen = VariableConstraintGenerator(rhs.attributeName, rule)
        val cons = gen.generate(NodeAttributes.fromAttr(NodeAttribute("a_is_decl", "true")))
        assertEquals(cons, listOf(BasicRuleConstraint(NodeAttribute(rhs.attributeName, "a"))))
    }
}