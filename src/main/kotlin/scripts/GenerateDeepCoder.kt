package scripts

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.constraints.BasicRuleConstraint
import grammars.deepcoder.DeepCoderInterpreter
import grammars.deepcoder.DeepCoderVariables
import grammars.deepcoder.FUNCTION_NAME
import grammars.deepcoder.deepCoderGrammar
import kotlin.random.Random

fun main(args: Array<String>) {
    val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 5, random = Random(12234))
    val strfier = ProgramStringifier()
    var numBad = 0
    var numRunnable = 0
    var numUseful = 0
    val nonUniformExceptions = mutableMapOf<String, MutableList<Pair<Exception, GenericGrammarNode>>>()
    for(i in 0..100) {
        val interpreter = DeepCoderInterpreter()
        val program = generator.generate(listOf(BasicRuleConstraint(NodeAttribute("length", "3"))))
        val inputVars = DeepCoderInterpreter.getInputs(program)
        val ioExamples = mutableListOf<Pair<String, String>>()
        try {
            for(i in 0..10){
                val input = DeepCoderVariables.fromInputs(inputVars)
                val output = DeepCoderInterpreter(input).interp(program)
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
        val useful = program.symbolCount(FUNCTION_NAME) > 0
        if(!useful) {
            continue
        }
        // Finally: Generate some IO pairs for it.
        println(ioExamples)

    }
    println("NUM BAD: ${numBad}")
    println("NUM RUNNABLE: ${numRunnable}")
    println("NUM WEIRD: ${nonUniformExceptions.values.size}")

}