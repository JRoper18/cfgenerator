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
    fun testInterpStmt() {
        //TODO: Get this to work in the case it creates a new variable.
        val generator = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 5, random = Random(1234))
        val stringifier = ProgramStringifier()
        repeat(100) {
            val interpreter = DeepCoderInterpreter()
            val program = makeStmt(generator)
            var interpErr = false
            try {
                interpreter.interpStmt(program)
            } catch (ex : DeepCoderInterpreter.InterpretError) {
                // Perhaps they passed in a lambda where a variable should be
                assert((interpreter.variables.intVars.size + interpreter.variables.listVars.size == 0))
                interpErr = true
            }
            if(!interpErr) {
                assert((interpreter.variables.intVars.size + interpreter.variables.listVars.size == 1) || DeepCoderInterpreter.getInputs(program).size == 1)
            }

        }
        repeat(100) {
            val interpreter = DeepCoderInterpreter()
            val program = makeStmt(generator)
            var interpErr = false
            try {
                interpreter.interp(stringifier.stringify(program))
            } catch (ex : DeepCoderInterpreter.InterpretError) {
                // Perhaps they passed in a lambda where a variable should be
                assert((interpreter.variables.intVars.size + interpreter.variables.listVars.size == 0))
                interpErr = true
            }
            if(!interpErr) {
                assert((interpreter.variables.intVars.size + interpreter.variables.listVars.size == 1) || DeepCoderInterpreter.getInputs(program).size == 1)
            }
        }

    }

    @Test
    fun testInterpString() {
        val dcVars = DeepCoderVariables(listVars = mutableMapOf("c" to listOf(-1, 1, 2, 3, 0), "u" to listOf()))
        val interpreter = DeepCoderInterpreter(dcVars)
        val progStr = "u:=[int]\n" +
                "c:=[int]\n" +
                "m:=Count (>0) c\n"
        val output = interpreter.interp(progStr)
        assertEquals("3", output)
    }

}