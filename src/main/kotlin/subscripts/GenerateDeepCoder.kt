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
import kotlin.random.Random
import java.io.File


fun generatePrograms(args: Array<String>) {
    val parser = ArgParser("example")
    println(args.joinToString(" "))
    val outputIn by parser.option(ArgType.String, shortName = "o", description = "Output file name")
    val numToMakeIn by parser.option(ArgType.Int, shortName = "n", description = "Number of examples to make")
//    val debugIn by parser.option(ArgType.Boolean, shortName = "d", description = "Turn on debug mode").default(false)
    parser.parse(args)

    val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(12234))
    val strfier = ProgramStringifier()
    val numToMake = numToMakeIn ?: 999999999
    val output = outputIn ?: "/dev/null"
    var numBad = 0
    var numRunnable = 0
    var numUseful = 0
    val nonUniformExceptions = mutableMapOf<String, MutableList<Pair<Exception, GenericGrammarNode>>>()
    File(output).printWriter().use { outF -> 
        for(i in 0..(numToMake-1)) {
            val interpreter = DeepCoderInterpreter()
            val program = generator.generate(listOf(BasicRuleConstraint(NodeAttribute("length", "5"))))
            val inputVars = DeepCoderInterpreter.getInputs(program)
            val ioExamples = mutableListOf<Pair<String, String>>()
            try {
                for(i in 0..10){
                    val input = DeepCoderVariables.fromInputs(inputVars)
                    val output = DeepCoderInterpreter(input.copy()).interp(program)
                    ioExamples.add(Pair(input.toString(), output))
                }
            } catch (e: DeepCoderInterpreter.ParseError) {
                //Whatever.
                numBad += 1
                continue
            } catch (e: Exception) {
                val key = e.javaClass.name
                nonUniformExceptions.putIfAbsent(key, mutableListOf<Pair<Exception, GenericGrammarNode>>())
                nonUniformExceptions[key]!!.add(Pair(e, program))
                continue
            }
            numRunnable += 1
            // Okay, now we have a good program. Is it useful?
            val useful = program.symbolCount(FUNCTION_NAME) >= 3
            if(!useful) {
                continue
            }
            numUseful += 1
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
    
    println("NUM USEFUL: ${numUseful}")
    println("NUM BAD: ${numBad}")
    println("NUM RUNNABLE: ${numRunnable}")
    println("NUM WEIRD: ${nonUniformExceptions.values.size}")

}