package grammars.lambda2

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class Lambda2InterpreterTest {

    @Test
    fun testToTree() {
        val interpreter = Lambda2Interpreter()
        val tr = interpreter.toTree(interpreter.tokenize("( lambda (  ) ( foldl ( lambda (    h    ) ( False   ) )  66   h    ) ) \n"))
        println(tr)
    }

    @Test
    fun testEval() {

    }
}