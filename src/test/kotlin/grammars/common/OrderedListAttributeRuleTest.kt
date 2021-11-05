package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OrderedListAttributeRuleTest {

    val lpr = ListProductionRule(NtSym("list"), NtSym("unit"))
    val rule = OrderedListAttributeRule(lpr, "key", listLength = 5)
    val unbound = OrderedListAttributeRule(lpr, "key", listLength = 2)

    @Test
    fun testCanMakeProgramWithAttributes() {
        val badRes = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("key", "anything")))
        assert(!badRes.first) // Should only make attributes with a numeral in them

        val unitTest = unbound.canMakeProgramWithAttributes(NodeAttributes.fromAttr(
            NodeAttribute(OrderedListAttributeRule.toAttrKey(0, "key"), "val")
        ))
        assert(unitTest.first)
        // 0th attributes have to be on the left if we assume no zero-length lists, because if it were on the RHS it would be a 1th.
        assertEquals(listOf(listOf(BasicRuleConstraint(NodeAttribute("0.key", "val"))), listOf(), listOf()), unitTest.second)

        val unitTest2 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(
            NodeAttribute(OrderedListAttributeRule.toAttrKey(0, "key"), "val")
        ))
        assert(unitTest2.first)
        // 0th attributes have to be on the left if we assume no zero-length lists, because if it were on the RHS it would be a 1th.
        assertEquals(listOf(listOf(BasicRuleConstraint(NodeAttribute("0.key", "val"))), listOf(), listOf()), unitTest2.second)

    }

    @Test
    fun testCanMakeBoundsCheck() {
        val res1 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("length", "6")))
        assert(!res1.first)

        val res2 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("length", "5")))
        assert(res2.first)

        val res3 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("length", "4"),
            NodeAttribute("5.key", "val")
        )))
        assert(!res3.first)

        val res4 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("length", "5"),
            NodeAttribute("5.key", "val") // Bad because args are 0-indexed
        )))
        assert(!res4.first)
    }

    @Test
    fun testCanMakeProgramWithAttributesBounded() {
        (1 until 5).forEach { maxDepth ->
            val allButLastAttrs = (0 until maxDepth).map {
                NodeAttribute(OrderedListAttributeRule.toAttrKey(it, "key"), it.toString())
            }
            val lastAttr = NodeAttribute(OrderedListAttributeRule.toAttrKey(maxDepth, "key"), maxDepth.toString())
            val wantedAttrs = NodeAttributes.fromList(allButLastAttrs + lastAttr + NodeAttribute("length", (maxDepth + 1).toString()))
            println(wantedAttrs)
            val res = rule.canMakeProgramWithAttributes(wantedAttrs)
            assert(res.first)
            val exp = allButLastAttrs.map {
                BasicRuleConstraint(it)
            } + BasicRuleConstraint(NodeAttribute("length", (maxDepth).toString()))
            println(exp)
            assertEquals(exp.toSet(), res.second[0].toSet())
            println(res)
            assertEquals(listOf(BasicRuleConstraint(NodeAttribute("key", maxDepth.toString()))), res.second[2])
        }
    }

    @Test
    fun testMakeSynthesizedExamples() {
        val listAttrs = NodeAttributes.fromList(listOf(
            NodeAttribute("0.key", "0"),
            NodeAttribute("1.key", "1")
        ))
        val unitAttr = NodeAttributes.fromAttr(NodeAttribute("key", "2"))

        val expAttrs = NodeAttributes.fromList(listOf(
            NodeAttribute("0.key", "0"),
            NodeAttribute("1.key", "1"),
            NodeAttribute("2.key", "2")
        ))
        val actualAttrs = rule.makeSynthesizedAttributes(listOf(listAttrs, NodeAttributes(), unitAttr))
        assertEquals(expAttrs, actualAttrs)

        // Try it with a single thing on the RHS
        val actual2 = rule.makeSynthesizedAttributes(listOf(NodeAttributes(), NodeAttributes(), unitAttr))
        assertEquals(NodeAttributes.fromAttr(NodeAttribute("0.key", "2")), actual2)

        // What about a single thing on the LHS?
        val actual3 = rule.makeSynthesizedAttributes(listOf(NodeAttributes.fromAttr(NodeAttribute("0.key", "2")),
            NodeAttributes(), NodeAttributes.fromAttr(NodeAttribute("key", "1"))))
        assertEquals(NodeAttributes.fromList(listOf(NodeAttribute("0.key", "2"), NodeAttribute("1.key", "1"))), actual3)

    }

    @Test
    fun testMakeSynthesizedAttributesGeneral() {
        (0 until 5).forEach { maxDepth ->
            val allButLastAttrs = (0 until maxDepth).map {
                NodeAttribute(OrderedListAttributeRule.toAttrKey(it, "key"), it.toString())
            }
            val lastAttr = NodeAttribute("key", maxDepth.toString())
            val newAttrs = rule.makeSynthesizedAttributes(listOf(
                NodeAttributes.fromList(allButLastAttrs),
                NodeAttributes(),
                NodeAttributes.fromAttr(lastAttr)))
            val expAttrs = NodeAttributes.fromList(allButLastAttrs + NodeAttribute(OrderedListAttributeRule.toAttrKey(maxDepth, "key"), maxDepth.toString()))
            assertEquals(expAttrs, newAttrs)
        }
    }

    @Test
    fun testCanMakeLengths() {
        val res1 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("0.key", "anything"),
            NodeAttribute("length", "2")
        )))

        assert(res1.first)

        val res2 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("1.key", "anything"),
            NodeAttribute("length", "1")
        )))

        assert(!res2.first)

        val res3 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("length", "1")
        )))

        // This rule can't make things of size 1
        assert(!res3.first)

        val res4 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("1.key", "anything"),
            NodeAttribute("length", "2")
        )))
        assert(res4.first)

        val res5 = rule.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(
            NodeAttribute("length", "2")
        )))
        assert(res5.first)
        assertEquals(res5.second, listOf(listOf(BasicRuleConstraint(NodeAttribute("length", "1"))), listOf(), listOf()))

    }

    @Test
    fun testInitRule() {
        val iRule = rule.initListRule(0, PR(NtSym("list"), listOf(StringSymbol("len1"))))
        val res1 = iRule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("length", "1")))
        assert(res1.first)
        assertEquals(res1.second, iRule.noConstraints)
    }

}