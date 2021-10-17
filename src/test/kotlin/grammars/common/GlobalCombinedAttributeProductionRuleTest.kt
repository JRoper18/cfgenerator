package grammars.common

import grammar.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class GlobalCombinedAttributeProductionRuleTest {

    val s1 = NtSym("L1")
    val s2 = NtSym("L2")
    val r1 = GlobalCombinedAttributeProductionRule(setOf(Regex("\\wtest")), APR(PR(s1, listOf(s2))))

    val a1 = NodeAttribute("atest", "true")
    val a2 = NodeAttribute("test", "true")

    @Test
    fun testMakeInheritedAttributes() {
        val syn1 = r1.makeSynthesizedAttributes(listOf(NodeAttributes.fromList(listOf(a1, a2))))
        assertEquals(NodeAttributes.fromList(listOf(a1)), syn1)
    }

    @Test
    fun testMakeSynthesizedAttributes() {
    }
}