package grammars.deepcoder

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammars.common.rules.UnexpandedAPR
import grammars.deepcoder.DeepCoderGrammar.FUNCTION_NAME
import grammars.deepcoder.DeepCoderGrammar.STMT
import grammars.deepcoder.DeepCoderGrammar.STMT_LIST
import grammars.deepcoder.DeepCoderGrammar.VARDEF
import grammars.deepcoder.DeepCoderGrammar.VARNAME
import grammars.deepcoder.DeepCoderGrammar.functionNameAttr
import grammars.deepcoder.DeepCoderGrammar.varAttrName

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random
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
    fun testGenerate() {
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1, random = Random(100L))

        repeat(10){
            val program = RootGrammarNode(UnexpandedAPR(STMT_LIST))
            val success = generator.expandNode(program, listOf(BasicRuleConstraint(NodeAttribute("length", "4"))))
            assert(success)
            program.verify()
            val progStr = (ProgramStringifier().stringify(program))
//            println(progStr)
//            println(program)
            assert(program.attributes().getStringAttribute("length")!!.toInt() == 4)

        }
    }

    @Test
    fun testGenerateFunctionNameWithConstraint() {
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
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
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
        val program = RootGrammarNode(UnexpandedAPR(FUNCTION_NAME))
        generator.expandNode(program)
        assertNotNull(program)
        val progStr = (ProgramStringifier().stringify(program))
        program.verify()
    }

    @Test
    fun testGenerateStatement() {
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
        val program = makeStmt(generator)
        val attrs = program.attributes()
//        println(program)
        assertNotNull(attrs.getStringAttribute("varName"))
        val progStr = (ProgramStringifier().stringify(program))
        assertContains(progStr, ":=")
        program.verify()
    }

    @Test
    fun testGenerateVarDecl() {
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
        val program = makeProgramFromSymbol(generator, VARNAME)
        val attrs = program.attributes()
        val varname = attrs.getStringAttribute(varAttrName)!!
        val varAttr = attrs.getStringAttribute(DeepCoderGrammar.VARDECL_RULE_BASE.makeAttrKeyFromVarname(varname))
        repeat(5) {
            assertNotNull(varAttr)
            assertEquals(DeepCoderGrammar.VARDECL_RULE_BASE.makeAttrValueFromChildren(NodeAttributes(), varname), varAttr)
        }
    }

    @Test
    fun testGenerateFunction() {
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
        val program = makeProgramFromSymbol(generator, VARDEF)
        assert(!program.isUnexpanded())

    }
}