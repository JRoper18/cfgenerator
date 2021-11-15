package grammars.common

import grammar.*
import grammars.common.rules.GlobalCombinedAttributeRule
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class GlobalCombinedAttributeRuleTest {

    val s1 = NtSym("L1")
    val s2 = NtSym("L2")
    val r1 = GlobalCombinedAttributeRule(setOf(Regex("\\wtest")), APR(PR(s1, listOf(s2))), false)
    val r2 = GlobalCombinedAttributeRule(setOf(Regex("\\wtest")), APR(PR(s1, listOf(s2))), true)

    val a1 = NodeAttribute("atest", "true")
    val a2 = NodeAttribute("test", "true")

    val attrs1 = NodeAttributes.fromList(listOf(a1, a2))
    val gattrs = NodeAttributes.fromList(listOf(a1))

    @Test
    fun testMakeInheritedAttributes() {
        for(rule in listOf(r1, r2)) { //Regardless of being a scope closer or not:
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

    }

    @Test
    fun testMakeSynthesizedAttributes() {
        val syn1 = r1.makeSynthesizedAttributes(listOf(attrs1))
        assertEquals(gattrs, syn1)

        // Scope closers don't do synthesized attrs
        assertEquals(NodeAttributes(), r2.makeSynthesizedAttributes(listOf(attrs1)))
    }
}