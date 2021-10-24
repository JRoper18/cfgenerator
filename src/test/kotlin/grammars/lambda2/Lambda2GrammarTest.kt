package grammars.lambda2

import generators.ProgramGenerator
import generators.ProgramStringifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class Lambda2GrammarTest {

    @Test
    fun testGenerate() {
        val generator = ProgramGenerator(Lambda2Grammar.grammar)
        val strfier = ProgramStringifier(tokenSeperator = " ")
        val interp = Lambda2Interpreter()
        repeat(5){
            val prog = generator.generate()
            val progStr = strfier.stringify(prog)
            println(prog)
            println(progStr)
//            println(interp.makeExamples(progStr, 10).map {
//                it.first + "\t" + it.second
//            }.joinToString ("\n"))
        }
    }
}