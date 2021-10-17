package grammars.deepcoder

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.NodeAttribute
import grammar.NtSym
import grammar.RootGrammarNode
import grammar.Symbol
import grammar.constraints.BasicRuleConstraint
import grammars.common.UnexpandedAPR

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

internal fun makeProgramFromSymbol(generator: ProgramGenerator, symbol: Symbol) : RootGrammarNode{
    val program = RootGrammarNode(UnexpandedAPR(symbol))
    val success = generator.expandNode(program)
    assert(success)
    assertNotNull(program)
    return program

}
internal fun makeStmt(generator: ProgramGenerator) : RootGrammarNode {
    return makeProgramFromSymbol(generator, STMT)
}

internal class DeepCoderGeneratorTest {

    @Test
    fun generate() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = RootGrammarNode(UnexpandedAPR(STMT_LIST))
        val success = generator.expandNode(program, listOf(BasicRuleConstraint(NodeAttribute("length", "3"))))
        println(program)
        assert(success)
        program.verify()
        println(program)
        val progStr = (ProgramStringifier().stringify(program))
        println(progStr)
        assert(program.attributes().getStringAttribute("length")!!.toInt() > 0)
    }

    @Test
    fun testGenerateFunctionNameWithConstraint() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val cons = listOf(BasicRuleConstraint(NodeAttribute(functionNameAttr, "Head")))
        val program = RootGrammarNode(UnexpandedAPR(FUNCTION_NAME))
        val success = generator.expandNode(program, cons)
        assert(success)
        assertNotNull(program)
        assertEquals("Head", program.attributes().getStringAttribute(functionNameAttr))
        val progStr = (ProgramStringifier().stringify(program))
        program.verify()
        assertContains(progStr, "Head")
    }

    @Test
    fun testGenerateFunctionNameWithoutConstraint() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = RootGrammarNode(UnexpandedAPR(FUNCTION_NAME))
        generator.expandNode(program)
        assertNotNull(program)
        val progStr = (ProgramStringifier().stringify(program))
        println(progStr)
        program.verify()
    }

    @Test
    fun testGenerateStatement() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = makeStmt(generator)
        val attrs = program.attributes()
        println(attrs)
        assertNotNull(attrs.getStringAttribute("varName"))
        val progStr = (ProgramStringifier().stringify(program))
        assertContains(progStr, ":=")
        program.verify()
    }

    @Test
    fun testGenerateVarDecl() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = makeProgramFromSymbol(generator, VARNAME)
        println(program.toString(true))
    }
}