package grammars.deepcoder

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.constraints.BasicRuleConstraint
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random

internal class DeepCoderInterpreterTest {

    @Test
    fun testInterp() {
    }

    @Test
    fun testInterpStmt() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(1234))
        val interpreter = DeepCoderInterpreter()
        val program = makeStmt(generator)
        println(program)
        interpreter.interpStmt(program)
        println(interpreter.variables)
        assert(interpreter.variables.intVars.size + interpreter.variables.listVars.size == 1)
    }

    @Test
    fun testParse() {
        val str = "p := [int]".trimIndent()
//        val prog = deepCoderGrammar.parse(str, STMT)

    }

}