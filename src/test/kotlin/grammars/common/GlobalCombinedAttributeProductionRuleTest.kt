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

    val attrs1 = NodeAttributes.fromList(listOf(a1, a2))
    val gattrs = NodeAttributes.fromList(listOf(a1))

    @Test
    fun testMakeInheritedAttributes() {
        val inh1 =r1.makeInheritedAttributes(0, NodeAttributes(), listOf(NodeAttributes(), attrs1))
        // Only our right sibling has the attrs so we shouldn't have them.
        assertNull(inh1.getStringAttribute(a1.first))
        val inh2 = r1.makeInheritedAttributes(1, NodeAttributes(), listOf(attrs1, NodeAttributes()))
        // Our left sibling has them: We should have them.
        assertEquals(gattrs, inh2)
        val inh3 = r1.makeInheritedAttributes(1, attrs1, listOf(NodeAttributes(), NodeAttributes()))
        // Our parent has them: We should have them.
        assertEquals(gattrs, inh3)

    }

    @Test
    fun testMakeSynthesizedAttributes() {
        val syn1 = r1.makeSynthesizedAttributes(listOf(attrs1))
        assertEquals(gattrs, syn1)
    }
}