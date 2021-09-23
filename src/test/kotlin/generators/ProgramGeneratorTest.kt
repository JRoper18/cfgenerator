package generators

import grammar.AttributeGrammar
import grammar.NodeAttribute
import grammar.NtSym
import grammar.StringSymbol
import grammar.constraints.BasicRuleConstraint
import grammars.common.InitAttributeProductionRule
import grammars.common.SizedListAttributeProductionRule
import grammars.common.TerminalProductionRule
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ProgramGeneratorTest {
    val listSym = NtSym("LIST")
    val unitSym = StringSymbol("UNIT")
    val rule = SizedListAttributeProductionRule(listSym, unitSym, " ")
    val grammar = AttributeGrammar(listOf(rule,
        InitAttributeProductionRule(TerminalProductionRule(listSym), "length", "0"),
    ), start = listSym, constraints = mapOf())
    val generator = ProgramGenerator(grammar)
    @Test
    fun testExpandNode() {
        val cons = listOf(BasicRuleConstraint(NodeAttribute("length", "1")))
        val program = generator.generate(cons)
        print(program)
        program.verify()
        println(ProgramStringifier().stringify(program!!))
    }
}