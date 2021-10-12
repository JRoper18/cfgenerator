package grammar

import grammars.deepcoder.deepCoderGrammar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class AttributeGrammarTest {
    @Test
    fun testToAntlr() {
        val antlr = (deepCoderGrammar.toAntlr("DeepCoder"))
        println(antlr)
        val lines = antlr.split("\n")
        var lIdx = 0
        File("./src/main/resources/DeepCoder.g4").forEachLine {
            assertEquals(lines[lIdx].trim(), it.trim())
            lIdx += 1
        }
    }
}