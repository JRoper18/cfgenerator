package subscripts

import grammar.GenericGrammarNode
import grammars.deepcoder.DeepCoderInterpreter
import grammars.deepcoder.DeepCoderVariables
import grammars.deepcoder.deepCoderGrammar
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

suspend fun evaluateDeepcoderPrograms(args: Array<String>) {
    // Given a program, see if it satisfies the input/output example.
    val parser = ArgParser("generate")
    println(args.joinToString(" "))
    val inputFileName by parser.option(
        ArgType.String,
        fullName = "input",
        shortName = "i",
        description = "Input file name"
    ).required()
    parser.parse(args)
    val evalExamples = File(inputFileName).readText().split("<|splitter|>")
    val numRunnableExamples = AtomicInteger(0)
    val numCorrectExamples = AtomicInteger(0)
    val numTotalExamples = AtomicInteger(0)
    val weirdMutex = Mutex()
    val weirdMap = mutableMapOf<String, MutableList<Pair<Exception, String>>>()
    evalExamples.pmap { example ->
        try {
            val splitExample = example.split("Program:")
            val programStr = splitExample[1].trim()
            val inputOutputExamples = splitExample[0].removePrefix("Examples:\n").trim().split("Inputs:")
            inputOutputExamples.forEach {
                numTotalExamples.incrementAndGet()
                val ioSplit = it.split("Output")
                val input = DeepCoderVariables(ioSplit[0].trim())
                val interpreter = DeepCoderInterpreter(input)
                val actualOutput = interpreter.interp(programStr)
                numRunnableExamples.incrementAndGet()
                val expectedOutput = ioSplit[1].trim()
                if(expectedOutput.trim() == actualOutput.trim()) {
                    numCorrectExamples.incrementAndGet()
                }
            }
        } catch (ex: Exception) {
            // Ripe for race conditions
            val key = ex.javaClass.name
            weirdMutex.withLock {
                weirdMap.putIfAbsent(key, mutableListOf<Pair<Exception, String>>())
                weirdMap[key]!!.add(Pair(ex, example))
            }
        }
    }
    println("NUM TOTAL: ${numTotalExamples.get()}")
    println("NUM RUNNABLE: ${numRunnableExamples.get()}")
    println("NUM CORRECT: ${numCorrectExamples.get()}")
    println("Exception map keys: ${weirdMap.keys}")
    println("Exception map values: ${weirdMap.values.first()[0].first}")
}