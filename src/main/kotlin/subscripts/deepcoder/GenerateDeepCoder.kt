package subscripts

import generators.ProgramGenerationResult
import generators.ProgramGenerationResult.PROGRAM_STATUS
import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammars.deepcoder.deepCoderGrammar
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * outputFileName is what it saves
 * makeUseful is a flag that's true if you want to only make/output useful programs
 * numToMake is the number to make
 * canSave is a filtering function that we'll use: We'll only save the result to memory if it passes
 * logOutputStream is where to put the log statements.
 * Returns a list of generated programs, filtered by your canSave function.
 */
suspend fun generateDeepcoderPrograms(
    makeUseful : Boolean,
    numToMake : Int,
    outputFileName: String = "/dev/null",
    canSaveToReturnMemory : (ProgramGenerationResult) -> Boolean = { false },
    logOutputStream: PrintWriter = PrintWriter(System.out, true)
) : List<ProgramGenerationResult> {
    val savedResults = java.util.Collections.synchronizedList(mutableListOf<ProgramGenerationResult>())
    val numBad = AtomicInteger(0)
    val numRunnable = AtomicInteger(0)
    val numUseful = AtomicInteger(0)
    val numExceptioned = AtomicInteger(0)
    val nonUniformExceptions = mutableMapOf<String, MutableList<Pair<Exception, GenericGrammarNode>>>()
    val numPerCoroutine = Math.ceil(numToMake.toDouble() / MAX_COROUTINES.toDouble()).toInt()
    val numCoroutines = if(makeUseful) MAX_COROUTINES else minOf(MAX_COROUTINES, numToMake)
    val strfier = ProgramStringifier()
    var doneFlag = false // Set to true if we're creating only useful programs and we've reached all the useful programs.
    val time = measureTimeMillis {
        val mutex = Mutex()
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(12234))
        File(outputFileName).printWriter().use { outF ->
            coroutineScope {
                repeat(numCoroutines) {
                    launch {
                        var num = 0
                        while((num < numPerCoroutine || makeUseful) && !doneFlag) {
                            num += 1
                            val generationResult = generateDeepcoderProgramAndExamples(generator = generator, nonUniformExceptions)
                            val program = generationResult.program
                            val examples = generationResult.examples

                            if(canSaveToReturnMemory(generationResult)) {
                                savedResults.add(generationResult)
                            }
                            if(generationResult.status == PROGRAM_STATUS.BAD) {
                                numBad.incrementAndGet()
                            } else if(generationResult.status == PROGRAM_STATUS.RUNNABLE) {
                                numRunnable.incrementAndGet()
                            } else {
                                numExceptioned.incrementAndGet()
                            }
                            
//                            val progStr = strfier.stringify(program).trim()
//                            logOutputStream.println(progStr)

                            // Okay, now we have a good program. Is it useful?
                            val useful = isDeepcoderProgramUseful(program, examples.size)
                            if(!useful) {
                                continue
                            }
                            val usefulNow = numUseful.incrementAndGet()
                            if(usefulNow >= numToMake && makeUseful) {
                                doneFlag = true // This mechanism can occasionally cause us to generate a few extra examples.
                                // That's fine, because that only happens if they're all generated at the same-ish time
                                // So we're not waiting a while, anyways.
                            }
                            mutex.withLock {
                                logOutputStream.println("Found useful ${usefulNow}!")
                                // Lock the file writing.
                                outF.println("<|splitter|>")
                                outF.print(generationResultToString(generationResult))
                            }
                        }
                    }
                }
            }
        }
    }
    logOutputStream.println("Time elapsed: ${time}ms")
    logOutputStream.println("NUM USEFUL: ${numUseful}")
    logOutputStream.println("NUM BAD: ${numBad}")
    logOutputStream.println("NUM RUNNABLE: ${numRunnable}")
    logOutputStream.println("NUM EXCEPTIONED: ${numExceptioned}")
    if(nonUniformExceptions.isNotEmpty()) {
        logOutputStream.println("NUM WEIRD: ${nonUniformExceptions.values.reduce { acc, mutableList ->
            (acc + mutableList).toMutableList()
        }.size}")
    }
    logOutputStream.println(nonUniformExceptions.values.map {
        it.map {
            it.first.stackTraceToString() + "\n" + strfier.stringify(it.second) + "\n" + it
        }
    })
    return savedResults.toList()
}
suspend fun generateDeepcoderProgramsCmd(args: Array<String>) {
    val parser = ArgParser("generate")
    println(args.joinToString(" "))
    val outputFileName by parser.option(ArgType.String, fullName = "output", shortName = "o", description = "Output file name").default("/dev/null")
    val numToMake by parser.option(ArgType.Int, shortName = "n", description = "Number of examples to make").default(1)
    val makeUseful by parser.option(ArgType.Boolean, fullName = "useful", description = "If true, we'll only count useful problems in the total count.").default(false)
    parser.parse(args)
    generateDeepcoderPrograms(outputFileName = outputFileName, makeUseful = makeUseful, numToMake = numToMake)

}