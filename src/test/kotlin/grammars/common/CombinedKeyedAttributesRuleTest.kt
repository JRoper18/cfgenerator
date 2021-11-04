package grammars.common

import grammar.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.lang.Exception

internal class CombinedKeyedAttributesRuleTest {
    val pr = ProductionRule(NtSym("left"), listOf(StringSymbol("end")))
    val attr1 = NodeAttribute("key1", "val1")
    val attr2 = NodeAttribute("key2", "val2")
    val r1 = InitAttributeProductionRule(pr, attr1.first, attr1.second)
    val r2 = InitAttributeProductionRule(pr, attr2.first, attr2.second)
    val t1 = r1.withOtherRule(r2)

    @Test
    fun testWontOverlapKeys() {
        assertThrows<IllegalArgumentException>() {
            val bad = r1.withOtherRule(r1)
        }
    }

    @Test
    fun testCanMakeProgramWithAttributes() {
        val res1 = t1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(attr1))
        assert(res1.first)
        assertEquals(res1.second, r1.noConstraints)
        val res2 = t1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(attr2))
        assert(res2.first)
        assertEquals(res2.second, r1.noConstraints)
        val res12 = t1.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(attr1, attr2)))
        assert(res12.first)
        assertEquals(res12.second, r1.noConstraints)

        val resBad = t1.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(NodeAttribute("key1", "val2"))))
        assert(!resBad.first)
    }

    @Test
    fun testCakeSynthesizedAttributes() {
        val attrs = t1.makeSynthesizedAttributes(listOf())
        assertEquals(NodeAttributes.fromList(listOf(attr1, attr2)), attrs)
    }

    @Test
    fun testCantMakeTooManyAttrs() {
        val resBad = t1.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(attr1, attr2, NodeAttribute("notthere", "dne"))))
        assert(!resBad.first)
    }

}