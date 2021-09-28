package grammars.common

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.StringsetSymbol
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringsetSymbolRuleTest {

    @Test
    fun testBasicStringSetRuleGenerations() {
        val STRINGSET = StringsetSymbol(setOf(
            "a",
            "b",
            "c",
            "d",
        ))
        val rules = makeStringsetRules(STRINGSET)
        assertEquals(rules.size, STRINGSET.stringset.size)
        rules.forEach {
            assertEquals(it.rule.rhs.size, 1)
            val canMakeData = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("chosenSymbol", it.rule.rhs[0].name))))
            assert(canMakeData.first)
            assert(canMakeData.second.isEmpty())
        }
    }

    @Test
    fun testAttributeNamedStringSetRuleGenerations() {
        val STRINGSET = StringsetSymbol(setOf(
            "a",
            "b",
            "c",
            "d",
        ), attributeName = "attrName")
        val rules = makeStringsetRules(STRINGSET)
        assertEquals(rules.size, STRINGSET.stringset.size)
        rules.forEach {
            assertEquals(it.rule.rhs.size, 1)
            val canMakeData = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("attrName", it.rule.rhs[0].name))))
            assert(canMakeData.first)
            assert(canMakeData.second.isEmpty())
        }
    }

    @Test
    fun testAttributedStringSetRuleGenerations() {
        val STRINGSET = StringsetSymbol(mapOf(
            "a" to setOf(NodeAttribute("in", "0")),
            "b" to setOf(NodeAttribute("in", "1")),
            "c" to setOf(NodeAttribute("in", "2")),
            "d" to setOf(NodeAttribute("in", "3")),
        ), attributeName = "attrName")
        val rules = makeStringsetRules(STRINGSET)
        assertEquals(rules.size, STRINGSET.stringset.size)
        rules.forEachIndexed { idx, it ->
            assertEquals(it.rule.rhs.size, 1)
            val canMakeData = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("attrName", it.rule.rhs[0].name))))
            assert(canMakeData.first)
            assert(canMakeData.second.isEmpty())
            val canMakeData2 = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("in", idx.toString()))))
            assert(canMakeData2.first)
            assert(canMakeData2.second.isEmpty())
            val canMakeData3 = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("in", (idx + 1).toString()))))
            assert(!canMakeData3.first)
            assert(canMakeData3.second.isEmpty())
        }
    }
}