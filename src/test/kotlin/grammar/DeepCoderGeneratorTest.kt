package grammar

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.constraints.BasicRuleConstraint
import grammars.common.UnexpandedAPR
import grammars.deepcoder.FUNCTION_NAME
import grammars.deepcoder.deepCoderGrammar

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

internal class DeepCoderGeneratorTest {

    @Test
    fun generate() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = RootGrammarNode(UnexpandedAPR(NtSym("Stmt")))
        val success = generator.expandNode(program)
        println(program)
        assert(success)
        println(program)
        println(ProgramStringifier().stringify(program!!))
        program.verify()
        assert(program.attributes().getStringAttribute("length")!!.toInt() > 0)
    }

    @Test
    fun testGenerateFunctionNameWithConstraint() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val cons = listOf(BasicRuleConstraint(NodeAttribute("chosenSymbol", "Head")))
//        val program = generator.generate(cons)
        val program = RootGrammarNode(UnexpandedAPR(FUNCTION_NAME))
        generator.expandNode(program, cons)
        println(program)
        assertNotNull(program)
        val progStr = (ProgramStringifier().stringify(program))
        program.verify()
        assertContains(progStr, "Head")
    }

    @Test
    fun testGenerateFunctionNameWithoutConstraint() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = RootGrammarNode(UnexpandedAPR(FUNCTION_NAME))
        generator.expandNode(program)
        println(program)
        assertNotNull(program)
        val progStr = (ProgramStringifier().stringify(program))
        program.verify()
    }

    @Test
    fun testGenerateStatement() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
//        val cons = listOf(BasicRuleConstraint(NodeAttribute("length", "3")))
//        val program = generator.generate(cons)
        val program = RootGrammarNode(UnexpandedAPR(NtSym("Stmt")))
        generator.expandNode(program)
        println(program)
        assertNotNull(program)
        println(ProgramStringifier().stringify(program))
        program.verify()
    }
}