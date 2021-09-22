package grammars.common

import grammar.*
import grammar.constraints.BasicRuleConstraint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class SizedListAttributeProductionRuleTest {
    val listSym = NtSym("LIST")
    val unitSym = StringSymbol("UNIT")
    val rule = SizedListAttributeProductionRule(listSym, unitSym, " ")

    @Test
    fun testCanMakeProgramWithAttribute() {
        val res1 = rule.canMakeProgramWithAttribute(NodeAttribute("length", "2"))
        assert(res1.first)
        assertEquals(res1.second, (listOf(BasicRuleConstraint(Pair("length", "1")))))
    }

    @Test
    fun testCannotMakeNegativeLengthList() {
        val res1 = rule.canMakeProgramWithAttribute(NodeAttribute("length", "-1"))
        assert(!res1.first)
        assert(res1.second.isEmpty())
    }

    @Test
    fun testCannotMakeNonLengthAttribute() {
        val res1 = rule.canMakeProgramWithAttribute(NodeAttribute("lenasdhas", "2"))
        assert(!res1.first)
        assert(res1.second.isEmpty())
    }

    @Test
    fun testMakeProgramWithAttribute() {
        val satisfyingProgram = RootGrammarNode(rule).withChildren {
            listOf(
                GrammarNode(InitAttributeProductionRule(TerminalProductionRule(unitSym), "length", "0"), it, 0)
            )
        }
        val res1 = rule.makeProgramWithAttribute(NodeAttribute("length", "1"), satisfyingProgram)
        assertNotNull(res1)
        val attrs = res1.attributes()
        res1.verify()
        assert(attrs.getStringAttribute("length").equals( "2"))
    }
}