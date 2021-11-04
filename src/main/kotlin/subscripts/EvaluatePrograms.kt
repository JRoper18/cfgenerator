package subscripts

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammars.Language
import grammars.deepcoder.*
import grammar.ProductionRule
import grammars.ProgramRunResult
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

suspend fun evaluateProgramsCmd(args: Array<String>) {
    // Given a program, see if it satisfies the input/output example.
    val parser = ArgParser("evaluate")
    println(args.joinToString(" "))
    val inputFileName by parser.option(
        ArgType.String,
        fullName = "input",
        shortName = "i",
        description = "Input file name of GPT-generated programs"
    ).required()
    val lanChoice by parser.option(ArgType.Choice<LanguageRef>(), shortName = "l", description = "Input language to generate").required()

    parser.parse(args)
    evaluatePrograms(argsToLanguage(lanChoice), File(inputFileName).readText().split("<|splitter|>").filter {
        it.isNotBlank()
    })

}


fun analyzeRuleFrequency(program : String, language : Language, frequencies : MutableMap<String, Int>) {
    language.grammar().rules.forEachIndexed { idx, rule -> 
        val toFind = "$idx {"
        val key = rule.rule.toString()
        val numOccurances = (program.length - (program.replace(toFind, "")).length) / toFind.length
        frequencies[key] = (frequencies[key] ?: 0) + numOccurances
    }
}
fun analyzeSymbolFrequency(program : String, language : Language, frequencies: MutableMap<String, Int>) {
    val stringsToFind = language.grammar().symbols.map {
        it.name
    }
    stringsToFind.forEach { toFind ->
        val numOccurances = (program.length - (program.replace(toFind, "")).length) / toFind.length
        frequencies[toFind] = (frequencies[toFind] ?: 0) + numOccurances
    }
}

suspend fun evaluatePrograms(language : Language, evalExamples : List<String>){
    val numProgramsWithExamples = AtomicInteger(0)
    val numFullyCorrectPrograms = AtomicInteger(0)
    val numTotalExamples = AtomicInteger(0)
    val runResultCounts = ProgramRunResult.values().map {
        Pair(it, AtomicInteger(0))
    }.toMap()
    val badSymFreqs = mutableMapOf<String, Int>()
    val goodSymFreqs = mutableMapOf<String, Int>()
    val goodRuleFreqs = mutableMapOf<String, Int>()
    val badRuleFreqs = mutableMapOf<String, Int>()
    evalExamples.pforall { example ->
        val splitExample = example.split("Program:")
        val programStr = splitExample[1].trim()
        var inputOutputExamples = splitExample[0].trim().removePrefix("Examples:").trim().split("Inputs:").filter {
            it.isNotBlank()
        }
        // Remove the first -- it may have been trimmed by the GPT model
        inputOutputExamples = inputOutputExamples.subList(1, inputOutputExamples.size)
        var gotOneRun = false
        var hitsAllExamples = true
        numTotalExamples.addAndGet(inputOutputExamples.size)
        if(inputOutputExamples.isNotEmpty()) {
            numProgramsWithExamples.incrementAndGet()
            inputOutputExamples.subList(1, inputOutputExamples.size).forEach {
                val ioSplit = it.split("Output:")
                val input = ioSplit[0].trim()
                val expectedOutput = ioSplit[1].trim()
                val runResult = language.runProgramAgainstExample(programStr, input, expectedOutput)
                gotOneRun = gotOneRun || runResult.result.finishedRun()
                hitsAllExamples = hitsAllExamples && runResult.result.isGood()
                runResultCounts[runResult.result]!!.incrementAndGet()
            }
            if(gotOneRun) {
                if(hitsAllExamples) {
                    numFullyCorrectPrograms.incrementAndGet()
                    analyzeSymbolFrequency(programStr, language, goodSymFreqs)
                    analyzeRuleFrequency(programStr, language, goodRuleFreqs)
                }
                else {
                    analyzeSymbolFrequency(programStr, language, badSymFreqs)
                    analyzeRuleFrequency(programStr, language, badRuleFreqs)
                }
            }
        }
    }
    println("NUM PROGRAMS: ${evalExamples.size}")
    println("NUM FULLY CORRECT PROGRAMS: ${numFullyCorrectPrograms.get()}")
    println(runResultCounts)
    println("Good frequencies: ")
    val search4Symbols = setOf("cons", "foldl", "foldr", "map", "recl", "filter", "+", "-", "*", "/", ">", "<", "or", "and")
    val finalGoodRules = FrequencyCounter(goodRuleFreqs, topK = 20)
    val finalBadRules = FrequencyCounter(badRuleFreqs, topK = 20)
    println(FrequencyCounter(goodSymFreqs, search4Symbols))
    println(finalGoodRules)
    println("Bad frequencies: ")
    println(FrequencyCounter(badSymFreqs, search4Symbols))
    println(finalBadRules)
    println("Biggest differences:")
    println("Mostly in good: ")
    println(finalGoodRules.freqDiff(finalBadRules))
    println("Mostly in bad: ")
    println(finalBadRules.freqDiff(finalGoodRules))

//    println("Exception map values: ${weirdMap.values.first()[0].first.stackTraceToString()}")
}
