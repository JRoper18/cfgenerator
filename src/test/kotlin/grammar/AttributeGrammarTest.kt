package grammar

import grammars.deepcoder.DeepCoderGrammar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class AttributeGrammarTest {
    @Test
    fun testToAntlr() {
        val antlr = (DeepCoderGrammar.grammar.toAntlr("DeepCoder"))
        println(antlr)
        val lines = antlr.grammarStr.split("\n")
        var lIdx = 0
        File("./src/test/resources/DeepCoder.g4").forEachLine {
            assertEquals(lines[lIdx].trim(), it.trim())
            lIdx += 1
        }
    }
}