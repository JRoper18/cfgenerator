package grammar

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammars.common.BoundedListProductionRule
import grammars.common.InitIntProductionRule
import grammars.common.TerminalProductionRule
import org.junit.jupiter.api.Test

internal class CommonGrammarTest {

    @Test
    fun generate() {
        val listSym = NtSym("List")
        val unitSym = StringSymbol("Unit")
        val grammar = AttributeGrammar(listOf(
            BoundedListProductionRule(listSym, unitSym, "\n", minimumSize = 1),
            InitIntProductionRule(TerminalProductionRule(listSym), "length", 0),
        ), listSym)
        val generator = ProgramGenerator()
        val program = (generator.generate(grammar))
        println(program)
        println(ProgramStringifier().stringify(program))
    }
}