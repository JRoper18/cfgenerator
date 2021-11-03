package grammars.lambda2

import grammars.ProgramGenerationResult
import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.AttributeGrammar
import grammar.RootGrammarNode
import grammars.Language
import grammars.ProgramRunResult
import kotlin.random.Random

open class Lambda2Language : Language {
    val interp = Lambda2Interpreter()
    val generator = ProgramGenerator(Lambda2Grammar.grammar)
    override fun generateProgramAndExamples(numExamples: Int): ProgramGenerationResult {
        val prog = generator.generate()
        val examples = interp.makeExamples(prog, numExamples)
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
        // If we're the identity, that's not useful either. 
       var isModifying = false
       for(example in examples) {
           if(example.first != example.second) {
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
        return interp.programToString(program)
    }

    override fun runProgramWithExample(program: String, input: String): String {
        return interp.interp(program, input)
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunResult {
        if(interp.hasSyntaxErr(program)) {
            return ProgramRunResult.PARSEERROR
        }
        try {
            return ProgramRunResult.fromBool(runProgramWithExample(program, input).trim() == output.trim())
        } catch (iex : Lambda2Interpreter.InterpretError) {
            return ProgramRunResult.RUNTIMEERROR
        }
    }

    override fun grammar(): AttributeGrammar {
        return Lambda2Grammar.grammar
    }
}