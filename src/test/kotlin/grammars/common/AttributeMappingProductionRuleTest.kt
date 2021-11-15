package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.FiniteAttributeMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class AttributeMappingProductionRuleTest {
    val ss = StringsetSymbol(setOf("a", "b", "c"))
    val mapper1 = FiniteAttributeMapper(mapOf("a" to "b", "b" to "c"))
    val r1 = AttributeMappingProductionRule(PR(NtSym("lhs"), listOf(ss)), ss.attributeName, 0, mapper1)

    @Test
    fun testCanMakeProgramWithAttributes() {
        val res1 = r1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "c")))
        assert(res1.first)
        assertEquals(res1.second, listOf(listOf(BasicRuleConstraint(NodeAttribute(ss.attributeName, "b")))))

        val res2 = r1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "b")))
        assert(res2.first)
        assertEquals(res2.second, listOf(listOf(BasicRuleConstraint(NodeAttribute(ss.attributeName, "a")))))

        val res3 = r1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "a")))
        assert(!res3.first)

    }

    @Test
    fun testMakeSynthesizedAttributes() {
        val attrs1 = r1.makeSynthesizedAttributes(listOf(NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "a"))))
        assertEquals(attrs1, NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "b")))

        val attrs2 = r1.makeSynthesizedAttributes(listOf(NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "b"))))
        assertEquals(attrs2, NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "c")))

        val attrs3 = r1.makeSynthesizedAttributes(listOf(NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, "c"))))
        assertEquals(attrs3, NodeAttributes.fromAttr(NodeAttribute(ss.attributeName, mapper1.default)))

    }
}