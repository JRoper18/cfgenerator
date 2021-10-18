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
        assertEquals(res1.second, listOf(listOf(BasicRuleConstraint(Pair("length", "1")))))
    }

    @Test
    fun testCannotMakeNegativeLengthList() {
        val res1 = rule.canMakeProgramWithAttribute(NodeAttribute("length", "-1"))
        assert(!res1.first)
        assert(res1.second.isEmpty())
    }

    @Test
    fun testCannotMakeZeroLengthList() {
        val res1 = rule.canMakeProgramWithAttribute(NodeAttribute("length", "0"))
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
        val satisfyingProgram = RootGrammarNode(InitAttributeProductionRule(TerminalProductionRule(listSym), "length", "0"))
        val res1 = rule.makeRootProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("length", "1")), listOf(satisfyingProgram))
        println(res1)
        assertNotNull(res1)
        val attrs = res1.attributes()
        res1.verify()
        assert(attrs.getStringAttribute("length").equals( "1"))
    }
}