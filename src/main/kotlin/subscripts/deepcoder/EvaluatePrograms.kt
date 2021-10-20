package subscripts

import generators.ProgramGenerator
import grammars.Language
import grammars.deepcoder.*
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
    evaluatePrograms(argsToLanguage(lanChoice), File(inputFileName).readText().split("<|splitter|>"))

}
suspend fun evaluatePrograms(language : Language, evalExamples : List<String>){
    val numRunnableExamples = AtomicInteger(0)
    val numCorrectExamples = AtomicInteger(0)
    val numCorrectPrograms = AtomicInteger(0)
    val numTotalExamples = AtomicInteger(0)
    val weirdMutex = Mutex()
    val weirdMap = mutableMapOf<String, MutableList<Pair<Exception, String>>>()

    evalExamples.pforall { example ->
        try {
            val splitExample = example.split("Program:")
            val programStr = splitExample[1].trim()
            val inputOutputExamples = splitExample[0].trim().removePrefix("Examples:").trim().split("Inputs:").filter {
                it.isNotBlank()
            }
            var hitsAllExamples = true
            inputOutputExamples.forEach {
                numTotalExamples.incrementAndGet()
                val ioSplit = it.split("Output:")
                val input = ioSplit[0].trim()
                val interpreter = DeepCoderInterpreter(input)
                val actualOutput = language.runProgramWithExample(programStr, input)
                numRunnableExamples.incrementAndGet()
                val expectedOutput = ioSplit[1].trim()
                if(expectedOutput.trim() == actualOutput.trim()) {
                    numCorrectExamples.incrementAndGet()
                }
                else {
                    hitsAllExamples = false;
                }
            }
            if(hitsAllExamples) {
                numCorrectPrograms.incrementAndGet()
            }
        } catch (ex: Exception) {
            // Ripe for race conditions
            val key = ex.javaClass.name
            weirdMutex.withLock {
                weirdMap.putIfAbsent(key, mutableListOf())
                weirdMap[key]!!.add(Pair(ex, example))
            }
        }
    }
    println("NUM TOTAL EXAMPLES: ${numTotalExamples.get()}")
    println("NUM RUNNABLE: ${numRunnableExamples.get()}")
    println("NUM CORRECT EXAMPLES: ${numCorrectExamples.get()}")
    println("NUM FULLY CORRECT PROGRAMS: ${numCorrectPrograms.get()}")
    println("Exception map keys: ${weirdMap.keys}")
//    println("Exception map values: ${weirdMap.values.first()[0].first.stackTraceToString()}")
}
