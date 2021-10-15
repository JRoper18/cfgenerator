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
        //TODO: Get this to work in the case it creates a new variable.
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(1234))
        val interpreter = DeepCoderInterpreter()
        val stringifier = ProgramStringifier()
        repeat(100) {
            val program = makeStmt(generator)
            interpreter.interpStmt(program)
            assert((interpreter.variables.intVars.size + interpreter.variables.listVars.size == 1) || DeepCoderInterpreter.getInputs(program).size == 1)

        }

    }

    @Test
    fun testParse() {
        val str = "p:=Maximum a b\nk:=[int]".trim()
        val prog = deepCoderGrammar.parse(str)
        println(prog)
        prog.verify()
    }

}