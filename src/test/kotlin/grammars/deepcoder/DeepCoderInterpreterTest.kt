package grammars.deepcoder

import generators.ProgramGenerator
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DeepCoderInterpreterTest {

    @Test
    fun testInterp() {
    }

    @Test
    fun testInterpStmt() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val interpreter = DeepCoderInterpreter()
        val program = makeStmt(generator)
        println(program)
        interpreter.interpStmt(program)
        println(interpreter.intVars)
        println(interpreter.listVars)
        assert(interpreter.intVars.size + interpreter.listVars.size == 1)
    }
}