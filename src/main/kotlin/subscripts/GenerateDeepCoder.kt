package subscripts

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.constraints.BasicRuleConstraint
import grammars.deepcoder.DeepCoderInterpreter
import grammars.deepcoder.DeepCoderVariables
import grammars.deepcoder.FUNCTION_NAME
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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

const val MAX_COROUTINES = 10

suspend fun generateDeepcoderPrograms(args: Array<String>) {
    val parser = ArgParser("generate")
    println(args.joinToString(" "))
    val outputFileName by parser.option(ArgType.String, fullName = "output", shortName = "o", description = "Output file name").default("/dev/null")
    val numToMake by parser.option(ArgType.Int, shortName = "n", description = "Number of examples to make").default(1)
    val makeUseful by parser.option(ArgType.Boolean, fullName = "useful", description = "If true, we'll only count useful problems in the total count.").default(false)
    parser.parse(args)
    var numBad = AtomicInteger(0)
    var numRunnable = AtomicInteger(0)
    var numUseful = AtomicInteger(0)
    val nonUniformExceptions = mutableMapOf<String, MutableList<Pair<Exception, GenericGrammarNode>>>()
    val numPerCoroutine = (numToMake / MAX_COROUTINES)
    val numCoroutines = if(makeUseful) MAX_COROUTINES else minOf(MAX_COROUTINES, numToMake)
    var doneFlag = false // Set to true if we're creating only useful programs and we've reached all the useful programs.
    val time = measureTimeMillis {
        val mutex = Mutex()
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(12234))
        val strfier = ProgramStringifier()
        File(outputFileName).printWriter().use { outF ->
            coroutineScope {
                repeat(numCoroutines) {
                    launch {
                        var num = 0
                        while((num < numPerCoroutine || makeUseful) && !doneFlag) {
                            num += 1
                            val program = generator.generate(listOf(BasicRuleConstraint(NodeAttribute("length", "3"))))
                            val inputVars = DeepCoderInterpreter.getInputs(program)
                            val ioExamples = mutableListOf<Pair<String, String>>()
                            try {
                                for(i in 0 until 10){
                                    val input = DeepCoderVariables.fromInputs(inputVars)
                                    val output = DeepCoderInterpreter(input.copy()).interp(program)
                                    if(output.trim() == "Null") {
                                        continue;
                                    }
                                    ioExamples.add(Pair(input.toString(), output))
                                }
                            } catch (e: DeepCoderInterpreter.InterpretError) {
                                //Whatever.
                                numBad.incrementAndGet()
                                continue
                            } catch (e: Exception) {
                                val key = e.javaClass.name
                                nonUniformExceptions.putIfAbsent(key, mutableListOf<Pair<Exception, GenericGrammarNode>>())
                                nonUniformExceptions[key]!!.add(Pair(e, program))
                                continue
                            }
                            numRunnable.incrementAndGet()
                            // Okay, now we have a good program. Is it useful?
                            val useful = program.symbolCount(FUNCTION_NAME) >= 1 && ioExamples.size > 0;
                            if(!useful) {
                                continue
                            }
                            val usefulNow = numUseful.incrementAndGet()
                            if(usefulNow >= numToMake && makeUseful) {
                                doneFlag = true // This mechanism can occasionally cause us to generate a few extra examples.
                                // That's fine, because that only happens if they're all generated at the same-ish time
                                // So we're not waiting a while, anyways.
                            }
                            val progStr = strfier.stringify(program).trim()
                            mutex.withLock {
                                println("Found a useful")
                                // Lock the file writing.
                                outF.println("<|splitter|>")
                                outF.println("Examples:")
//                                println(progStr)
                                ioExamples.forEach {
                                    outF.println("Inputs: ")
                                    outF.println(it.first)
                                    outF.println("Output: ")
                                    outF.println(it.second)
                                }
                                outF.println()
                                outF.println("Program: ")
                                outF.println(progStr)
                            }
                        }
                    }
                }
            }
        }
    }
    println("Time elapsed: ${time}ms")
    println("NUM USEFUL: ${numUseful}")
    println("NUM BAD: ${numBad}")
    println("NUM RUNNABLE: ${numRunnable}")
    println("NUM WEIRD: ${nonUniformExceptions.values.size}")

}