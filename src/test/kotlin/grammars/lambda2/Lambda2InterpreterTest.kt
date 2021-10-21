package grammars.lambda2

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class Lambda2InterpreterTest {

    @Test
    fun testFoldl() {
        val interp = Lambda2Interpreter()
        val prog = "lambda x : foldl ( lambda a , b: ( a ) + ( b ) , 0 , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("15", output)
    }

    @Test
    fun testFoldr() {
        val interp = Lambda2Interpreter()
        val prog = "lambda x : foldr( lambda a , b: ( a ) + ( b ) , 0 , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("15", output)
    }

    @Test
    fun testFilter() {
        val interp = Lambda2Interpreter()
        val prog = "lambda x : filter ( lambda a : ( a ) > ( 2 ) , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("[3, 4, 5]", output)
    }

    @Test
    fun testMap() {
        val interp = Lambda2Interpreter()
        val prog = "lambda x : map ( lambda a : ( a ) * ( 2 ) , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("[2, 4, 6, 8, 10]", output)
    }
}