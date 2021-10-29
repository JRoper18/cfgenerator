package grammars.common

import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.RootGrammarNode
import grammar.StringSymbol
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InitAttributeProductionRuleTest {

    val sym = StringSymbol("Symb")
    val rule = InitAttributeProductionRule(TerminalProductionRule(sym), "attr", "0")

    @Test
    fun testCanMakeProgramWithAttribute() {
        val res1 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("attr", "0")))
        assert(res1.first)
        assert(res1.second.isEmpty())
    }

    @Test
    fun testCannotMakeProgramWithAttribute() {
        val res1 = rule.canMakeProgramWithAttributes(NodeAttributes.fromAttr((NodeAttribute("attr", "1"))))
        assert(!res1.first)
        assert(res1.second.isEmpty())
    }

    @Test
    fun testMakeProgramWithAttribute() {
        val res1 = rule.makeRootProgramWithAttributes(NodeAttributes.fromAttr(NodeAttribute("attr", "0")), listOf())
        val attrs = res1.attributes()
        res1.verify()
        assert(attrs.getStringAttribute("attr").equals( "0"))
    }
}