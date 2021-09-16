package grammar

import generators.ProgramGenerator
import grammars.deepcoder.deepCoderGrammar
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DeepCoderGeneratorTest {

    @Test
    fun generate() {
        val generator = ProgramGenerator(numRandomTries = 1)
        println(generator.generate(deepCoderGrammar))
    }
}