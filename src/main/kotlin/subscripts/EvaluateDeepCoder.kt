package subscripts

import grammar.GenericGrammarNode
import grammars.deepcoder.deepCoderGrammar
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

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
    val numRunnable = AtomicInteger(0)
    val numTotal = evalExamples.size
    val mutex = Mutex()
    evalExamples.parallelStream().forEach { example ->
        val splitExample = example.split("Program:")
        val inputOutput = splitExample[0].split("Output: ")
        val inputStr = splitExample[0].removePrefix("Examples:\n").trim()
        val outputStr = splitExample[1].trim()
        val programStr = splitExample[1]
        val program = deepCoderGrammar.parse(programStr)
        println(inputStr)
        println(outputStr)
    }
}