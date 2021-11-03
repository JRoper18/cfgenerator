package grammars.common

import OrderedSynthesizedAttributeRule
import grammar.*
import grammar.constraints.BasicRuleConstraint
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OrderedSynthesizedAttributeRuleTest {
    val key1 = "key1"
    val key2 = "key2"
    val r1 = OrderedSynthesizedAttributeRule(setOf(Pair(key1, 1), Pair(key2, 0)),
        PR(NtSym("lhs"), listOf(StringSymbol("end1"), StringSymbol("end2"))))

    val r2 = OrderedSynthesizedAttributeRule(setOf(Pair(key1, 1), Pair(key1, 0)),
        PR(NtSym("lhs"), listOf(StringSymbol("end1"), StringSymbol("end2"))))
    @Test
    fun testMakeSynthesizedAttributes() {
        val synthed1 = r1.makeSynthesizedAttributes(listOf(
            NodeAttributes.fromAttr(NodeAttribute(key2, "a")),
            NodeAttributes.fromAttr(NodeAttribute(key1, "b")))
        )
        assertEquals(NodeAttributes.fromList(listOf(
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key2, 0), "a"),
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 1), "b"),
        )), synthed1)


        val synthed2 = r2.makeSynthesizedAttributes(listOf(
            NodeAttributes.fromAttr(NodeAttribute(key1, "a")),
            NodeAttributes.fromAttr(NodeAttribute(key1, "b")))
        )
        assertEquals(NodeAttributes.fromList(listOf(
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 0), "a"),
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 1), "b"),
        )), synthed2)

        val synthed3 = r2.makeSynthesizedAttributes(listOf(
            NodeAttributes.fromAttr(NodeAttribute(key1, "a")),
            NodeAttributes()
        ))
        assertEquals(NodeAttributes.fromList(listOf(
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 0), "a"),
        )), synthed3)
    }

    @Test
    fun testCanMakeProgramWithAttributes() {
        val res1 = r1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 1), "x")
        ))
        assert(res1.first)
        assertEquals(res1.second, listOf(listOf(), listOf(BasicRuleConstraint(NodeAttribute(key1, "x")))))

        val res2 = r1.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 1), "x"),
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key2, 0), "x")
        )))
        assert(res2.first)
        assertEquals(res2.second, listOf(listOf(BasicRuleConstraint(NodeAttribute(key2, "x"))),
            listOf(BasicRuleConstraint(NodeAttribute(key1, "x")))))

        val resNot = r1.canMakeProgramWithAttributes(NodeAttributes.fromAttr(
            NodeAttribute(OrderedSynthesizedAttributeRule.makeAttrKey(key1, 0), "x")
        ))
        assert(!resNot.first)
    }
}