package subscripts

import grammars.ProgramGenerationResult
import grammars.ProgramGenerationResult.PROGRAM_STATUS
import grammars.Language
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
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
suspend fun generatePrograms(
    language : Language,
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
    val numPerCoroutine = Math.ceil(numToMake.toDouble() / MAX_COROUTINES.toDouble()).toInt()
    val numCoroutines = if(makeUseful) MAX_COROUTINES else minOf(MAX_COROUTINES, numToMake)
    var doneFlag = false // Set to true if we're creating only useful programs and we've reached all the useful programs.
    val time = measureTimeMillis {
        val mutex = Mutex()
        File(outputFileName).printWriter().use { outF ->
            coroutineScope {
                repeat(numCoroutines) {
                    launch {
                        var num = 0
                        while((num < numPerCoroutine || makeUseful) && !doneFlag) {
                            num += 1
                            val generationResult = language.generateProgramAndExamples(7)

                            if(canSaveToReturnMemory(generationResult)) {
                                savedResults.add(generationResult)
                            }
                            when (generationResult.status) {
                                PROGRAM_STATUS.BAD -> {
                                    numBad.incrementAndGet()
                                }
                                PROGRAM_STATUS.RUNNABLE -> {
                                    numRunnable.incrementAndGet()
                                }
                                else -> {
                                    numExceptioned.incrementAndGet()
                                }
                            }

                            // Okay, now we have a good program. Is it useful?
                            logOutputStream.println(generationResult.examples)
                            logOutputStream.println(language.programToString(generationResult.program))
                            val useful = language.isProgramUseful(generationResult)
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
                                outF.print(generationResultToString(language, generationResult))
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
    return savedResults.toList()
}

suspend fun generateProgramsCmd(args: Array<String>) {
    val parser = ArgParser("generate")
    println(args.joinToString(" "))
    val lanChoice by parser.option(ArgType.Choice<LanguageRef>(), shortName = "l", description = "Input language to generate").required()
    val outputFileName by parser.option(ArgType.String, fullName = "output", shortName = "o", description = "Output file name").default("/dev/null")
    val numToMake by parser.option(ArgType.Int, shortName = "n", description = "Number of examples to make").default(1)
    val makeUseful by parser.option(ArgType.Boolean, fullName = "useful", description = "If true, we'll only count useful problems in the total count.").default(false)
    parser.parse(args)
    generatePrograms(argsToLanguage(lanChoice), outputFileName = outputFileName, makeUseful = makeUseful, numToMake = numToMake)

}