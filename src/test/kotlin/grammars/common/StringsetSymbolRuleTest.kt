package grammars.common

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.StringsetSymbol
import grammar.constraints.RuleConstraint
import grammars.common.rules.makeStringsetRules
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringsetSymbolRuleTest {

    internal fun areNoConstraints(childConsLists : List<List<RuleConstraint>>) : Boolean {
        return childConsLists.flatten().isEmpty()
    }

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
            assert(areNoConstraints(canMakeData.second))
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
            assert(areNoConstraints(canMakeData.second))
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
            assert(areNoConstraints(canMakeData.second))
            val canMakeData2 = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("in", idx.toString()))))
            assert(canMakeData2.first)
            assert(areNoConstraints(canMakeData.second))
            val canMakeData3 = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("in", (idx + 1).toString()))))
            assert(!canMakeData3.first)
            assert(areNoConstraints(canMakeData.second))
        }
    }
}