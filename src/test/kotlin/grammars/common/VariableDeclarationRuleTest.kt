package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class VariableDeclarationRuleTest {

    val r = StringSymbol("R")
    val rule = VariableDeclarationRule(NtSym("L"), r, "varname")
    val hasAttr = NodeAttribute("r${VariableDeclarationRule.DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX}", "true")
    val needsAttr = NodeAttribute("varname", "r")

    @Test
    fun testMakeSynthesizedAttributes() {
        val synthesizedAttrs = rule.makeSynthesizedAttributes(listOf(NodeAttributes.fromAttr(needsAttr)))
        assertEquals(synthesizedAttrs, NodeAttributes.fromAttr(hasAttr))

    }


    @Test
    fun testCanMakeProgramWithAttributes() {
        val res = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(hasAttr))
        assert(res.first)
        assertEquals(res.second, listOf(listOf(BasicRuleConstraint(needsAttr))))
        val res2 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("x${VariableDeclarationRule.DECLARED_VARS_ATTRIBUTE_KEY_SUFFIX}", "true")))
        assert(res2.first)
        assertEquals(res2.second, listOf(listOf(BasicRuleConstraint(NodeAttribute("varname", "x")))))

    }

    @Test
    fun testMakeChildrenForAttributes() {
        val canMake1 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(hasAttr))
        println(canMake1)
    }
}