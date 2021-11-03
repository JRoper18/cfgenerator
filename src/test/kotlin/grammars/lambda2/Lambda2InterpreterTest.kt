package grammars.lambda2

import grammar.RootGrammarNode
import grammars.deepcoder.DeepCoderInterpreter
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

internal class Lambda2InterpreterTest {

    val interp = Lambda2Interpreter()

    internal fun testIO(prog : String, input : String, output : String) {
        val actual = interp.interp(prog, input)
        assertEquals(output, actual)
    }

    @Test
    fun testFoldl() {
        val prog = "lambda x : foldl ( lambda a , b: ( a ) + ( b ) , 0 , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("15", output)
    }

    @Test
    fun testFoldr() {
        val prog = "lambda x : foldr( lambda a , b: ( a ) + ( b ) , 0 , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("15", output)
    }

    @Test
    fun testFilter() {
        val prog = "lambda x : filter ( lambda a : ( a ) > ( 2 ) , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("[3, 4, 5]", output)
    }

    @Test
    fun testMap() {
        val prog = "lambda x : map ( lambda a : ( a ) * ( 2 ) , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("[2, 4, 6, 8, 10]", output)
    }

    @Test
    fun testCons() {
        val prog1 = "lambda x : cons ( 1 , x ) "
        testIO(prog1, "[2, 3]", "[1, 2, 3]")
        assertThrows<Lambda2Interpreter.InterpretError>() {
            interp.interp(prog1, "[True, False]")
        }
    }

    @Test
    fun testConcat() {
        val prog1 = "lambda x , y : concat ( x , y ) "
        testIO(prog1, "[2, 3],[1, 4]", "[2, 3, 1, 4]")
        assertThrows<Lambda2Interpreter.InterpretError>() {
            interp.interp(prog1, "[True, False], [1, 3]")
        }
    }

    @Test
    fun testInsert() {
        val prog1 = "lambda x , y , z : insert ( x , y , z ) "
        testIO(prog1, "0, 42, [1, 2, 3]", "[42, 1, 2, 3]")
        testIO(prog1, "1, 42, [1, 2, 3]", "[1, 42, 2, 3]")
        testIO(prog1, "4, 42, [1, 2, 3]", "[1, 2, 3, 42]")
        assertThrows<Lambda2Interpreter.InterpretError>() {
            interp.interp(prog1, "4, True, [1, 2, 3]")
        }
    }

    @Test
    fun testSanity() {
        val prog1 = "lambda r    : r  "
        testIO(prog1, "True", "True")
    }

    @Test
    fun testCanMakeExamples() {
    }
}