package languages.deepcoder

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.AttributeGrammar
import grammar.NodeAttribute
import grammar.RootGrammarNode
import grammar.constraints.BasicRuleConstraint
import grammars.deepcoder.DeepCoderGrammar
import grammars.deepcoder.DeepCoderInterpreter
import grammars.deepcoder.DeepCoderVariables
import languages.*
import kotlin.random.Random

class DeepcoderLanguage(val progLength : Int = 5) : Language<DeepCoderVariables, String>{
    val generator = ProgramGenerator(DeepCoderGrammar.grammar, random = Random(System.currentTimeMillis()))
    val strfier = ProgramStringifier()
    override fun generateProgramAndExamples(numExamples: Int, config : GenerationConfig): ProgramGenerationResult<DeepCoderVariables, String> {
        val program = generator.generate(listOf(BasicRuleConstraint(NodeAttribute("length", "5"))), config)
        val inputVars = DeepCoderInterpreter.getInputs(program)
        val ioExamples = mutableListOf<Pair<DeepCoderVariables, String>>()
        val errors : MutableMap<String, MutableList<Exception>> = mutableMapOf()
        try {
            for(i in 0 until numExamples){
                val input = DeepCoderVariables.fromInputs(inputVars)
                val output = DeepCoderInterpreter(input.copy()).interp(program)
                if(output.trim() == "Null") {
                    continue;
                }
                ioExamples.add(Pair(input, output))
            }
        } catch (e: DeepCoderInterpreter.InterpretError) {
            //Whatever.
            return ProgramGenerationResult(program, ioExamples.toList(), ProgramGenerationResult.PROGRAM_STATUS.BAD, errors)
        } catch (e: Exception) {
            val key = e.javaClass.name
            errors.putIfAbsent(key, mutableListOf())
            errors[key]!!.add(e)
            return ProgramGenerationResult(program, ioExamples.toList(), ProgramGenerationResult.PROGRAM_STATUS.EXCEPTIONED, errors)
        }
        return ProgramGenerationResult(program, ioExamples.toList(), ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE, errors)
    }



    override fun isProgramUseful(result: ProgramGenerationResult<DeepCoderVariables, String>): Boolean {
        return result.program.symbolCount(DeepCoderGrammar.FUNCTION_NAME) >= 3
                && result.examples.isNotEmpty()
                && result.status == ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE
    }

    override fun programToString(program: RootGrammarNode): String {
        return strfier.stringify(program)
    }

    override fun runProgramWithExample(program: String, input: String): String {
        return DeepCoderInterpreter(DeepCoderVariables(input)).interp(program)
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        try {
            val actual = runProgramWithExample(program, input).trim()
            return ProgramRunDetailedResult.fromInputOutput(input, actual, output)
        } catch (ex : Exception) {
            return ProgramRunDetailedResult(ProgramRunResult.RUNTIMEERROR, ex.localizedMessage)
        }
    }

    override fun grammar(): AttributeGrammar {
        return DeepCoderGrammar.grammar
    }

    override fun exampleToString(example: Pair<DeepCoderVariables, String>): Pair<String, String> {
        return Pair(example.first.toString(), example.second)
    }

    override suspend fun preprocessOnExamples(program: String, examples: Collection<Pair<String, String>>): String {
        return program
    }

}