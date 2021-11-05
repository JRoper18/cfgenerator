package grammar

import generators.ProgramGenerator
import grammars.lambda2.Lambda2Grammar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class AttributeGrammarTest {
    @Test
    fun testToAntlr() {
//        val antlr = (deepCoderGrammar.toAntlr("DeepCoder"))
//        val lines = antlr.grammarStr.split("\n")
//        var lIdx = 0
//        File("./src/main/antlr/DeepCoder/DeepCoder.g4").forEachLine {
//            assertEquals(lines[lIdx].trim(), it.trim())
//            lIdx += 1
//        }
    }


    @Test
    fun testEncodeDecode() {
        val gen = ProgramGenerator(Lambda2Grammar.grammar)
        repeat(10) {
            val orig = gen.generate(listOf())
            val encoded = Lambda2Grammar.grammar.encode(orig, Regex("retType"))
            val remade = Lambda2Grammar.grammar.decode(encoded)[0]
            remade.verify()
            assertEquals(orig.toString(), remade.toString())
        }
    }

}