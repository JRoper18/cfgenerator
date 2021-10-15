package grammars.deepcoder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DeepCoderVariablesTest {

    @Test
    fun testStringConstructor() {
        val input = "m = [-11, 9, 16, -15]\n" +
                "i = [16, 4, -15, 2, -1]\n"
        val vars = DeepCoderVariables(input)
        assert(vars.hasVar("m"))
        assert(vars.hasVar("i"))
        assertEquals(vars.listVars["m"], listOf(-11, 9, 16, -15))

    }
}