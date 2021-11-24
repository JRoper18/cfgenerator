package grammars.lambda2

import interpreters.common.ProgramState
import languages.TypedFunctionalLanguage
import languages.TypedFunctionalLanguage.ExampleRunData
import languages.lambda2.Lambda2
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

internal class Lambda2PropertyTest {

    val language = Lambda2.language

    @Test
    fun testSubprogramExamplesCaptured() {
        val prog = "lambda x : plus ( 1 , plus ( x , 2 ) ) "
        val out1 = language.interpFull(prog, "1")
        assertEquals(out1.input, listOf(1))
        assertEquals(out1.output, 4)
        // The 0th arg takes no inputs and returns 1
        assertEquals(listOf(ExampleRunData(listOf(), 1)), out1.subprogramExamples[0])
        assertEquals(
            listOf(
                // The 1st arg takes no inputs and returns 3
                ExampleRunData(listOf(), 3, subprogramExamples = listOf(
                    listOf(ExampleRunData(listOf(), 1)), // and the 0th of the 1st is x (which is 1)
                    listOf(ExampleRunData(listOf(), 2)) // and the 1st of the 1st is 2 (which is a constant)
                )),
        ), out1.subprogramExamples[1])
    }

    @Test
    fun testSubprogramLambdaCaptured() {
        val subprog = "lambda y : times ( y , 2 )"
        val prog = "lambda x : map ( $subprog , x )"
        val out1 = language.interpFull(prog, "[3, 4]")
        assertEquals(out1.input, listOf(listOf(3, 4)))
        assertEquals(out1.output, listOf(6, 8))
        val lambdaExamples1 = listOf(language.interpFull(subprog, "3"), language.interpFull(subprog, "4"))
        assertEquals(lambdaExamples1, out1.subprogramExamples[0])
    }
}