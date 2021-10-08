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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

const val MAX_COUROUTINES = 10

suspend fun generateDeepcoderPrograms(args: Array<String>) {
    val parser = ArgParser("example")
    println(args.joinToString(" "))
    val outputIn by parser.option(ArgType.String, shortName = "o", description = "Output file name")
    val numToMakeIn by parser.option(ArgType.Int, shortName = "n", description = "Number of examples to make")
//    val debugIn by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)
    parser.parse(args)
    var numBad = AtomicInteger(0)
    var numRunnable = AtomicInteger(0)
    var numUseful = AtomicInteger(0)
    val numToMake = numToMakeIn ?: 999999999
    val nonUniformExceptions = mutableMapOf<String, MutableList<Pair<Exception, GenericGrammarNode>>>()
    val numPerCoroutine = (numToMake / MAX_COUROUTINES).toInt()
    val time = measureTimeMillis {
        val outputFileName = outputIn ?: "/dev/null"
        val mutex = Mutex()
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(12234))
        val strfier = ProgramStringifier()
        File(outputFileName).printWriter().use { outF ->
            coroutineScope {
                repeat(minOf(MAX_COUROUTINES, numToMake)) {
                    launch {
                        for(num in 0 until numPerCoroutine) {
                            val program = generator.generate(listOf(BasicRuleConstraint(NodeAttribute("length", "3"))))
                            val inputVars = DeepCoderInterpreter.getInputs(program)
                            val ioExamples = mutableListOf<Pair<String, String>>()
                            try {
                                for(i in 0 until 10){
                                    val input = DeepCoderVariables.fromInputs(inputVars)
                                    val output = DeepCoderInterpreter(input.copy()).interp(program)
                                    ioExamples.add(Pair(input.toString(), output))
                                }
                            } catch (e: DeepCoderInterpreter.ParseError) {
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
                            val useful = program.symbolCount(FUNCTION_NAME) >= 2
                            if(!useful) {
                                continue
                            }
                            numUseful.incrementAndGet()
                            mutex.withLock {
                                // Lock the file writing.
                                outF.println("Program: ")
                                outF.println(strfier.stringify(program).trim())
                                outF.println()
                                outF.println("Examples:")
                                println("Found a useful")
                                ioExamples.forEach {
                                    outF.println("Inputs: ")
                                    outF.println(it.first)
                                    outF.println("Output: ")
                                    outF.println(it.second)
                                }
                                outF.println()
                                outF.println()
                                outF.println()
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