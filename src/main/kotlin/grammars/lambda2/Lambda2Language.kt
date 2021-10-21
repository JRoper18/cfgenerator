package grammars.lambda2

import generators.ProgramGenerationResult
import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.AttributeGrammar
import grammar.RootGrammarNode
import grammars.Language
import kotlin.random.Random

class Lambda2Language : Language {
    val interp = Lambda2Interpreter()
    val generator = ProgramGenerator(Lambda2Grammar.grammar, random = Random(System.currentTimeMillis()))
    val strfier = ProgramStringifier(" ")
    override fun generateProgramAndExamples(numExamples: Int): ProgramGenerationResult {
        val prog = generator.generate()
        val progStr = programToString(prog)
        val examples = interp.makeExamples(progStr, numExamples)
        val status = if(examples.isEmpty()) ProgramGenerationResult.PROGRAM_STATUS.BAD else ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE
        return ProgramGenerationResult(prog, examples, status)
    }

    override fun isProgramUseful(result: ProgramGenerationResult): Boolean {
        val examples = result.examples
        val outputs = examples.map {
            it.second
        }
        // If outputs are all the same, the program just returns a constant or something. Not useful. 
        if(outputs.toSet().size == 1){
            return false
        }
        // If we're returning an input arg unmodified, that's not useful either. 
        // We'll check for this by checking for every example, if the output is a substring the input.
        var isModifying = false
        for(example in examples) {
            if(!example.first.contains(example.second)) {
                isModifying = true
                break
            }
        }
        if(!isModifying) {
            return false
        }
        return result.examples.isNotEmpty() && result.status == ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE
    }

    override fun programToString(program: RootGrammarNode): String {
        return strfier.stringify(program)
    }

    override fun runProgramWithExample(program: String, input: String): String {
        return interp.interp(program, input)
    }

    override fun grammar(): AttributeGrammar {
        return Lambda2Grammar.grammar
    }
}