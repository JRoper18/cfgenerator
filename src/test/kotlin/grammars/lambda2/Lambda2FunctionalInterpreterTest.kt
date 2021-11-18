package grammars.lambda2

import languages.TypedFunctionalLanguage
import languages.lambda2.Lambda2
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

internal class Lambda2FunctionalInterpreterTest {

    val interp = Lambda2.language

    internal fun testIO(prog : String, input : String, output : Any) {
        val actual = interp.interp(prog, input)
        assertEquals(output, actual)
    }

    @Test
    fun testFoldl() {
        val prog = "lambda x : foldl ( lambda a , b : ( a ) + ( b ) , 0 , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("15", output)
    }

    @Test
    fun testFoldr() {
        val prog = "lambda x : foldr( lambda a , b : ( a ) + ( b ) , 0 , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals("15", output)
    }

    @Test
    fun testFilter() {
        val prog = "lambda x : filter ( lambda a : gt ( a , 2 ) , x ) "
        val output = interp.interp(prog, "[1, 2, 3, 4, 5]")
        assertEquals(listOf(3, 4, 5), output)
    }

    @Test
    fun testMap() {
        val prog1 = "lambda x : map ( lambda a : times ( a , 2 ) , x ) "
        val prog2 = "lambda x : map ( lambda a : plus ( a , 2 ) , x ) "
        testIO(prog1, "[1, 2, 3, 4, 5]", listOf(2, 4, 6, 8, 10))
        testIO(prog2, "[1, 2, 3, 4, 5]", listOf(3, 4, 5, 6, 7))

    }

    @Test
    fun testCons() {
        val prog1 = "lambda x : cons ( 1 , x ) "
        testIO(prog1, "[2, 3]", listOf(1, 2, 3))
        assertThrows<TypedFunctionalLanguage.InterpretError>() {
            interp.interp(prog1, "[true, false]")
        }
    }

    @Test
    fun testConcat() {
        val prog1 = "lambda x , y : concat ( x , y ) "
        testIO(prog1, "[2, 3],[1, 4]", listOf(2, 3, 1, 4))
        assertThrows<TypedFunctionalLanguage.InterpretError>() {
            interp.interp(prog1, "[true, false], [1, 3]")
        }
    }

    @Test
    fun testInsert() {
        val prog1 = "lambda x , y , z : insert ( x , y , z ) "
        testIO(prog1, "0, 42, [1, 2, 3]", "[42, 1, 2, 3]")
        testIO(prog1, "1, 42, [1, 2, 3]", "[1, 42, 2, 3]")
        testIO(prog1, "4, 42, [1, 2, 3]", "[1, 2, 3, 42]")
        assertThrows<TypedFunctionalLanguage.InterpretError>() {
            interp.interp(prog1, "4, True, [1, 2, 3]")
        }
    }

    @Test
    fun testSanity() {
        val prog1 = "lambda r    : r  "
        testIO(prog1, "true", true)
    }

}