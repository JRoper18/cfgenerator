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
        repeat(5){
            val prog = generator.generate()
//            println(prog)
            val progStr = strfier.stringify(prog)
            println(progStr)
            println(Lambda2Interpreter().interp(progStr))

        }
    }
}