package grammars.common.interpreters

import grammars.common.mappers.WrapperAttributeMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ConsFunctionTest {

    @Test
    fun execute() {
        val f = ConsFunction("list", WrapperAttributeMapper())
        assertEquals(listOf("a", "b", "c"), f.execute(listOf("a", listOf("b", "c"))))
    }
}