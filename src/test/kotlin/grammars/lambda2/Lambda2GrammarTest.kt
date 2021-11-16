package grammars.lambda2

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.ProductionRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class Lambda2GrammarTest {

    val strfier = ProgramStringifier(tokenSeperator = " ")
    val grammar = Lambda2FunctionalLanguage.language.grammar
    @Test
    fun testGenerate() {
        val generator = ProgramGenerator(grammar, random = Random(40L), numRandomTries = 5)

        println(grammar.globalAttributeRegexes)
        repeat(5){
            val prog = generator.generate()
            val progStr = strfier.stringify(prog)
            println(progStr)
            println(Lambda2FunctionalLanguage.language.makeExamples(prog, 3).map {
                "${it.first} \t ${it.second} : ${it.second.javaClass.name}"
            }.joinToString ("\n"))
        }
    }


    @Test
    fun testCanMakeAllRules() {
        val generator = ProgramGenerator(grammar, numRandomTries = 5)

        // Can every rule be generated?
        val givenRulesSet = grammar.givenRules.map {
            it.rule
        }.toSet()
        val generatedRules = mutableSetOf<ProductionRule>()
        for(i in 0 until 100) {
            val prog = generator.generate()
            prog.forEachInTree {
                val r = it.productionRule.rule
                if(r in givenRulesSet) {
                    generatedRules.add(r)
                }
            }
            if(generatedRules.size == givenRulesSet.size){
                println("Found early")
                break // done early
            }
        }

        // Turn them to strings so that our junit output is cleaner if it fails.
        val generatedStr = generatedRules.filter {
            it in givenRulesSet // Only worry about given rules
            // Stringset/generated rules are too much
        }.map {
            it.toString()
        }.sorted().joinToString("\n")
        val expectedRulesStr = grammar.givenRules.map {
            it.rule.toString()
        }.sorted().joinToString("\n")
        assertEquals(expectedRulesStr, generatedStr)
    }

}