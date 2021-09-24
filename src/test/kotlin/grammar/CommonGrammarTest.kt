package grammar

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammars.common.InitAttributeProductionRule
import grammars.common.TerminalProductionRule
import grammars.common.makeStringsetRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class CommonGrammarTest {
    private val STRINGSET = StringsetSymbol(setOf(
        "a",
        "b",
        "c",
        "d",
    ))

    @Test
    fun testStringSetRuleGenerations() {
        val rules = makeStringsetRules(STRINGSET)
        assertEquals(rules.size, STRINGSET.stringset.size)
        rules.forEach {
            assertEquals(it.rule.rhs.size, 1)
            val canMakeData = it.canMakeProgramWithAttributes(NodeAttributes.fromList(listOf(Pair("chosenSymbol", it.rule.rhs[0].name))))
            assert(canMakeData.first)
            assert(canMakeData.second.isEmpty())
        }
    }
}