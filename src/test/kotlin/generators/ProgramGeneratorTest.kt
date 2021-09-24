package generators

import grammar.AttributeGrammar
import grammar.NodeAttribute
import grammar.NtSym
import grammar.StringSymbol
import grammar.constraints.BasicRuleConstraint
import grammars.common.InitAttributeProductionRule
import grammars.common.SizedListAttributeProductionRule
import grammars.common.TerminalProductionRule
import grammars.deepcoder.deepCoderGrammar
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

    @Test
    fun testDeterminism() {
        // Just use deepcoder, it's simple but not so simple that it's inherently deterministic.
        val gen1 = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val gen2 = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val stringifier = ProgramStringifier()
        assertEquals(stringifier.stringify(gen1.generate(listOf())), stringifier.stringify(gen2.generate(listOf())))
        assertEquals(stringifier.stringify(gen1.generate(listOf())), stringifier.stringify(gen2.generate(listOf())))
        assertEquals(stringifier.stringify(gen1.generate(listOf())), stringifier.stringify(gen2.generate(listOf())))

    }
}