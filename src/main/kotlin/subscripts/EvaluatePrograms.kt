package subscripts

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import languages.Language
import languages.ProgramRunResult
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.*
import java.util.concurrent.atomic.AtomicInteger

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
    val outputLogName by parser.option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Output log file path"
    )
    val outputExampleDir by parser.option(
        ArgType.String,
        fullName = "examples",
        shortName = "e",
        description = "A directory to put good and bad runs of each program. "
    )
    val lanChoice by parser.option(ArgType.Choice<LanguageRef>(), shortName = "l", description = "Input language to generate").required()

    parser.parse(args)
    val logWriter : PrintWriter
    if(outputLogName == null){
        println("Output file: to STDOUT")
        logWriter = PrintWriter(System.out, true)
    } else {
        println("Output file: $outputLogName")
        logWriter = PrintWriter(FileOutputStream(outputLogName), true)
    }
    val exampleWriter : PrintWriter
    if(outputExampleDir == null){
        println("Examples file: to NOWHERE")
        exampleWriter = PrintWriter(OutputStream.nullOutputStream(), true)
    } else {
        println("Examples file: $outputExampleDir")
        exampleWriter = PrintWriter(FileOutputStream(outputExampleDir), true)
    }
    println("Input file: $inputFileName")
    evaluatePrograms(argsToLanguage(lanChoice), File(inputFileName).readText().split("<|splitter|>").filter {
        it.isNotBlank()
    }, logWriter, exampleWriter)

}


fun analyzeRuleFrequency(program : String, language : Language<*, *>, frequencies : MutableMap<String, Int>) {
    language.grammar().rules.forEachIndexed { idx, rule ->
        val toFind = "$idx {"
        val key = rule.rule.toString()
        val numOccurances = (program.length - (program.replace(toFind, "")).length) / toFind.length
        frequencies[key] = (frequencies[key] ?: 0) + numOccurances
    }
}
fun analyzeSymbolFrequency(program : String, language : Language<*, *>, frequencies: MutableMap<String, Int>) {
    val stringsToFind = language.grammar().symbols.map {
        it.name
    }
    stringsToFind.forEach { toFind ->
        val numOccurances = (program.length - (program.replace(toFind, "")).length) / toFind.length
        frequencies[toFind] = (frequencies[toFind] ?: 0) + numOccurances
    }
}
data class ProgramEvaluationResult(val runResults : List<ProgramRunResult>, val symbolCounts : Map<String, Int>, val programExampleString : String) {
    fun successRatio(): Double {
        val ratio = (runResultCounts[ProgramRunResult.SUCCESS] ?: 0).toDouble()/ runResults.size
        return ratio
    }
    val runResultCounts : Map<ProgramRunResult, Int> by lazy {
        runResults.countMap(ProgramRunResult.values().toList())
    }
}
suspend fun gradeAttempt(language : Language<*, *>, attempt : String) : ProgramEvaluationResult {
    val exLines = attempt.lines()
    val nameLines = exLines.filter {
        it.contains("Name: ")
    }
    val name : String?
    if(nameLines.isNotEmpty()) {
        name = nameLines[0] + "\n"
    } else {
        name = "\n"
    }
    val splitExample = attempt.split("Program:")
    val initialProgramStr = splitExample[1].trim()
    var inputOutputExamples = splitExample[0].trim().removePrefix("Examples:").trim().split("Inputs:").filter {
        it.isNotBlank()
    }
    val runResults = mutableListOf<ProgramRunResult>()
    val programExampleStr = StringBuilder()
    // Remove the first -- it may have been trimmed by the GPT model, and it also may contain a "Name: <name>"
    // inputOutputExamples = inputOutputExamples.subList(1, inputOutputExamples.size)
    var gotOneRun = false
    var hitsAllExamples = true
    val symbolCounts = mutableMapOf<String, Int>()
    if(inputOutputExamples.isNotEmpty()) {
        programExampleStr.append(name)
        val finalExamples = inputOutputExamples.map {
            val ioSplit = it.split("Output:")
            val input = ioSplit[0].trim()
            val expectedOutput = ioSplit[1].trim()
            Pair(input, expectedOutput)
        }
        val preprocessTimeoutMs = 10 * 1000L
        val programStr = withTimeoutOrNull(preprocessTimeoutMs) {
            language.preprocessOnExamples(initialProgramStr, finalExamples)
        } ?: if (true) {
            println(initialProgramStr)
            language.bareMinimumPreprocessing(initialProgramStr, finalExamples)
        } else {
            initialProgramStr
        }
        finalExamples.forEach {
            val input = it.first
            val expectedOutput = it.second
            val runResult = language.runProgramAgainstExample(programStr, input, expectedOutput)
            gotOneRun = gotOneRun || runResult.result.finishedRun()
            hitsAllExamples = hitsAllExamples && runResult.result.isGood()
            runResults.add(runResult.result)
            programExampleStr.append("Inputs :\n")
            programExampleStr.append(input)
            programExampleStr.append("\nExpected Output: \n")
            programExampleStr.append(expectedOutput)
            programExampleStr.append("\nResult: ${runResult.result}\n")
            val resMsg = runResult.message.trim()
            if (resMsg.isNotBlank()) {
                programExampleStr.append("${resMsg}\n")
            }
        }
        programExampleStr.append("Program:\n${programStr}\n")
        try {
            analyzeSymbolFrequency(programStr, language, symbolCounts)
        } catch (ex : NotImplementedError) {
            // Whatever. Maybe it doesn't have a grammar.
        }
    }
    return ProgramEvaluationResult(runResults = runResults.toList(), symbolCounts = symbolCounts, programExampleString = programExampleStr.toString())
}

data class AggregateEvaluationResult(
    val numPrograms: Int,
    val numFullyCorrectPrograms: Int,
    val runResults: List<ProgramRunResult>,
    val goodSymFreqs : Map<String, Int>,
    val badSymFreqs : Map<String, Int>
) {
    val runResultCounts = runResults.countMap(ProgramRunResult.values().toList())
    override fun toString() : String {
        val out = StringWriter()
        val logWriter = PrintWriter(out)
        logWriter.println("NUM PROGRAMS: ${numPrograms}")
        logWriter.println("NUM FULLY CORRECT PROGRAMS: ${numFullyCorrectPrograms}")
        logWriter.println(runResultCounts)
        logWriter.println("Good frequencies: ")
        val goodSymbolsFreq = FrequencyCounter(goodSymFreqs)
        val badSymbolsFreq = FrequencyCounter(badSymFreqs)
        logWriter.println(goodSymbolsFreq)
        logWriter.println("Bad frequencies: ")
        logWriter.println(badSymbolsFreq)
        logWriter.println("Biggest differences:")
        logWriter.println("Mostly in good: ")
        logWriter.println(goodSymbolsFreq.freqDiff(badSymbolsFreq))
        logWriter.println("Mostly in bad: ")
        logWriter.println(badSymbolsFreq.freqDiff(goodSymbolsFreq))
        logWriter.flush()
        return out.toString()
    }
}

suspend fun evaluatePrograms(language : Language<*, *>, evalExamples : List<String>, logWriter : PrintWriter, exampleWriter : PrintWriter){
    val numFullyCorrectPrograms = AtomicInteger(0)
    val orderedRunResults = mutableListOf<ProgramRunResult>()
    val badSymFreqs = mutableMapOf<String, Int>()
    val goodSymFreqs = mutableMapOf<String, Int>()
    val goodRuleFreqs = mutableMapOf<String, Int>()
    val badRuleFreqs = mutableMapOf<String, Int>()
    evalExamples.forEach { example ->
        val attempts = example.split("<|attempt|>").filter {
            it.isNotBlank()
        }
        val attemptEvalResults = attempts.map { gradeAttempt(language, it) }
        val bestResult = attemptEvalResults.reduce { acc, programEvaluationResult ->
            val newSuccessRatio = programEvaluationResult.successRatio()
            val oldSuccessRatio = acc.successRatio()
            if(newSuccessRatio > oldSuccessRatio) {
                programEvaluationResult
            } else {
                acc
            }
        }
        orderedRunResults.addAll(bestResult.runResults)
        exampleWriter.println(bestResult.programExampleString)
        if(bestResult.successRatio() == 1.0) {
            numFullyCorrectPrograms.incrementAndGet()
            bestResult.symbolCounts.forEach {
                goodSymFreqs.compute(it.key) { oldKey, oldVal ->
                    (oldVal ?: 0) + it.value
                }
            }
        }
        else {
            bestResult.symbolCounts.forEach {
                badSymFreqs.compute(it.key) { oldKey, oldVal ->
                    (oldVal ?: 0) + it.value
                }
            }
        }
    }
    val gson = GsonBuilder().setPrettyPrinting().create()
    val aggResult = AggregateEvaluationResult(
        numPrograms = evalExamples.size,
        numFullyCorrectPrograms = numFullyCorrectPrograms.get(),
        runResults = orderedRunResults,
        goodSymFreqs = goodSymFreqs,
        badSymFreqs = badSymFreqs,
    )
    val jsonResultsStr = gson.toJson(aggResult)
    logWriter.println(jsonResultsStr)


//    println("Exception map values: ${weirdMap.values.first()[0].first.stackTraceToString()}")
}
