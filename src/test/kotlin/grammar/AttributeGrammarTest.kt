package grammar

import grammars.deepcoder.deepCoderGrammar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AttributeGrammarTest {
    @Test
    fun testToAntlr() {
        println(deepCoderGrammar.toAntlr("DeepCoder"))
    }
}