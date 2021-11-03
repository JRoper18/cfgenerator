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

    private fun splitProgramInputs(input : String) : List<String> {
        val args = mutableListOf<String>()
        var builtArg = ""
        var depth = 0
        for(ch in input) {
            if(ch == '[') {
                depth += 1
            }
            else if(ch == ']') {
                depth -= 1
            }
            
            if(ch == ',' && depth == 0) {
                args.add(builtArg.toString())
                builtArg = ""
            }
            else {
                builtArg += ch
            }
        }
        if(builtArg != "") {
            args.add(builtArg)
        }
        return args.toList().map {
            it.trim()
        }
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
            val inputs = splitProgramInputs(example.first).toSet()
            // No identities and no returning unmodified inputs
            if(example.first != example.second && !inputs.contains(example.second.trim())) {
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