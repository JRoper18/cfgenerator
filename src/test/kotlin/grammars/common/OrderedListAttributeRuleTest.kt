package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OrderedListAttributeRuleTest {

    val lpr = ListProductionRule(NtSym("list"), NtSym("unit"))
    val rule = OrderedListAttributeRule(lpr, "key")

    @Test
    fun testCanMakeProgramWithAttributes() {
        (0 until 5).forEach { maxDepth ->
            val allButLastAttrs = (0 until maxDepth).map {
                NodeAttribute(OrderedListAttributeRule.toAttrKey(it, "key"), it.toString())
            }
            val lastAttr = NodeAttribute(OrderedListAttributeRule.toAttrKey(maxDepth, "key"), maxDepth.toString())
            val wantedAttrs = NodeAttributes.fromList(allButLastAttrs + lastAttr)
            println(wantedAttrs)
            val res = rule.canMakeProgramWithAttributes(wantedAttrs)
            assert(res.first)
            assertEquals(allButLastAttrs.map {
                BasicRuleConstraint(it)
            }, res.second[0])
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

}