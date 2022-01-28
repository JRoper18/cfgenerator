package languages.lambda2

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.NodeAttribute
import grammar.ProductionRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class Lambda2GrammarTest {

    val strfier = ProgramStringifier(tokenSeperator = " ")
    val grammar = Lambda2.language.grammar
    @Test
    fun testGenerate() {
        val generator = ProgramGenerator(grammar, random = Random(42L), numRandomTries = 5, maxProgramDepth = 5)
        repeat(5){
            val prog = generator.generate()
            val progStr = strfier.stringify(prog)
            println(progStr)
            println(prog)
            println(Lambda2.language.makeExamples(prog, 3).map {
                "${it.input} \t ${it.output} : ${it.output.javaClass.name}"
            }.joinToString ("\n"))


//            val genRes = Lambda2.language.generateProgramAndExamples(3)
//            println(CfgLanguage(Lambda2.language).generationResultToString(genRes))
        }
    }


    @Test
    fun testCanMakeAllRules() {
        val generator = ProgramGenerator(grammar, timeoutMs = 1000L, returnPartialOnTimeout = true, random = Random(42L), numRandomTries = 5)

        // Can every rule be generated?
        val givenRulesSet = grammar.givenRules.map {
            it.rule
        }.toSet()
        val generatedRules = mutableSetOf<ProductionRule>()
        for(i in 0 until 100) {
            println("made $i")
            val prog = generator.generate()
            prog.forEachInTree {
                val r = it.productionRule.rule
                if(r in givenRulesSet) {
                    generatedRules.add(r)
                }
            }
            println("Missing ${givenRulesSet - generatedRules}")
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

    @Test
    fun testCanMakeAllFunctions() {
        val lan = Lambda2FunctionalLanguage(random = Random(31L))
        val fnames = lan.functions.keys
        val maxTries = 20
        val numEx = 3
        fnames.forEach { functionName ->
            var couldMake = false
            println(functionName)
            for(i in 0 until maxTries) {
                val prog = lan.makeLambdaWithStmt(varnames = listOf("a", "q", "v"), functionName)
                println(lan.programToString(prog))
                try {
                    prog.verify()
                } catch (ex : Exception) {
                    // Try again.
                    continue
                }
                val ex = lan.makeExamples(prog, numEx)
                if(ex.size == numEx) {
                    println(ex.joinToString("\n"))
                    couldMake = true
                    break
                }
            }
            assertTrue(couldMake)
        }
    }

}