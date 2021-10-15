package subscripts

import generators.ProgramGenerationResult
import generators.ProgramGenerator
import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.RootGrammarNode
import grammar.constraints.BasicRuleConstraint
import grammars.deepcoder.DeepCoderInterpreter
import grammars.deepcoder.DeepCoderVariables
import grammars.deepcoder.FUNCTION_NAME
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


suspend fun <A> Iterable<A>.pforall(f: suspend (A) -> Unit) = coroutineScope {
    map {
        async {
            f(it)
            true
        }
    }.awaitAll()
}

fun isDeepcoderProgramUseful(program: RootGrammarNode, numWorkingExamples : Int) : Boolean {
    return program.symbolCount(FUNCTION_NAME) >= 1 && numWorkingExamples > 0;
}
fun generateDeepcoderProgramAndExamples(generator: ProgramGenerator,
                                        generationExceptions: MutableMap<String, MutableList<Pair<Exception, GenericGrammarNode>>> = mutableMapOf(),
                                        numExamples : Int = 10,

                                        ) : ProgramGenerationResult {
    val program = generator.generate(listOf(BasicRuleConstraint(NodeAttribute("length", "3"))))
    val inputVars = DeepCoderInterpreter.getInputs(program)
    val ioExamples = mutableListOf<Pair<String, String>>()
    try {
        for(i in 0 until numExamples){
            val input = DeepCoderVariables.fromInputs(inputVars)
            val output = DeepCoderInterpreter(input.copy()).interp(program)
            if(output.trim() == "Null") {
                continue;
            }
            ioExamples.add(Pair(input.toString(), output))
        }
    } catch (e: DeepCoderInterpreter.InterpretError) {
        //Whatever.
        return ProgramGenerationResult(program, ioExamples.toList(), ProgramGenerationResult.PROGRAM_STATUS.BAD)
    } catch (e: Exception) {
        val key = e.javaClass.name
        generationExceptions.putIfAbsent(key, mutableListOf<Pair<Exception, GenericGrammarNode>>())
        generationExceptions[key]!!.add(Pair(e, program))
        return ProgramGenerationResult(program, ioExamples.toList(), ProgramGenerationResult.PROGRAM_STATUS.EXCEPTIONED)
    }
    return ProgramGenerationResult(program, ioExamples.toList(), ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE)
}