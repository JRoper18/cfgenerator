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

    @Test
    fun testExpandNodeList() {
        val listSym = NtSym("LIST")
        val unitSym = StringSymbol("UNIT")
        val rule = SizedListAttributeProductionRule(listSym, unitSym, " ")
        val grammar = AttributeGrammar(listOf(rule,
            InitAttributeProductionRule(TerminalProductionRule(listSym), "length", "0"),
        ), start = listSym, constraints = mapOf())
        val generator = ProgramGenerator(grammar)

        val cons = listOf(BasicRuleConstraint(NodeAttribute("length", "1")))
        val program = generator.generate(cons)
        print(program)
        program.verify() // Will throw exception if the program is wrong.
        println(ProgramStringifier().stringify(program))
    }

//    fun testExpandNodeLookup() {
//        val listSym = NtSym("LIST")
//        val unitSym = StringSymbol("UNIT")
//        val rule = SizedListAttributeProductionRule(listSym, unitSym, " ")
//        val grammar = AttributeGrammar(listOf(rule,
//            InitAttributeProductionRule(TerminalProductionRule(listSym), "length", "0"),
//        ), start = listSym, constraints = mapOf())
//        val generator = ProgramGenerator(grammar)
//    }
}