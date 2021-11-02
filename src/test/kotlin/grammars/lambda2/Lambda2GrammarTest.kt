package grammars.lambda2

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.ProductionRule
import grammars.common.UnexpandedAPR
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class Lambda2GrammarTest {

    val generator = ProgramGenerator(Lambda2Grammar.grammar)
    val strfier = ProgramStringifier(tokenSeperator = " ")
    val interp = Lambda2Interpreter()

    @Test
    fun testGenerate() {
        println(Lambda2Grammar.grammar.globalAttributeRegexes)
        repeat(5){
            val prog = generator.generate()
            println(prog)
            val progStr = strfier.stringify(prog)
            println(progStr)
            println(interp.makeExamples(prog, 3).map {
                it.first + "\t" + it.second
            }.joinToString ("\n"))
        }
    }

    @Test
    fun testCanMakeAllRules() {
        // Can every rule be generated?
        val generatedRules = mutableSetOf<ProductionRule>()
        repeat(100) {
            val prog = generator.generate()
            prog.forEachInTree {
                generatedRules.add(it.productionRule.rule)
            }
        }
        // Turn them to strings so that our junit output is cleaner if it fails.
        val givenRulesSet = Lambda2Grammar.grammar.givenRules.map {
            it.rule
        }.toSet()
        val generatedStr = generatedRules.filter {
            it in givenRulesSet // Only worry about given rules
            // Stringset/generated rules are too much
        }.map {
            it.toString()
        }.sorted().joinToString("\n")
        val expectedRulesStr = Lambda2Grammar.grammar.givenRules.map {
            it.rule.toString()
        }.sorted().joinToString("\n")
        assertEquals(expectedRulesStr, generatedStr)
    }

}