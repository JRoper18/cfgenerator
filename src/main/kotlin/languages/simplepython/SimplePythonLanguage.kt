package languages.simplepython

import generators.ProgramGenerator
import grammar.AttributeGrammar
import grammar.RootGrammarNode
import grammars.lambda2.Lambda2Grammar
import interpreters.simplepython.PythonInterpreter
import languages.Language
import languages.ProgramGenerationResult
import languages.ProgramRunDetailedResult
import languages.ProgramRunResult

/*
abstract class SimplePythonLanguage : Language {
    val interp = PythonInterpreter()
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
            val inputs = splitProgramInputs(example.first as String).toList()
            // No identities and no returning unmodified inputs
            if(example.first != example.second && !inputs.contains((example.second as String).trim())) {
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
        TODO()
    }

    override fun runProgramWithExample(program: String, input: String): String {
        return interp.interp(program, input)
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        if(interp.hasSyntaxErr(program)) {
            return ProgramRunDetailedResult(ProgramRunResult.PARSEERROR, "")
        }
        try {
            val actual = runProgramWithExample(program, input).trim()
            return ProgramRunDetailedResult.fromInputOutput(input, actual, output.trim())
        } catch (iex : PythonInterpreter.InterpretError) {
            val localMsg = iex.localizedMessage
            val rrs : ProgramRunResult;
            if(localMsg.contains("TypeError")) {
                rrs = ProgramRunResult.TYPEERROR
            }
            else if(localMsg.contains("NameError")) {
                rrs = ProgramRunResult.NAMEERROR
            }
            else {
                rrs = ProgramRunResult.RUNTIMEERROR
            }
            return ProgramRunDetailedResult(rrs, iex.localizedMessage)
        }
    }

    override fun grammar(): AttributeGrammar {
        TODO("this")
        return Lambda2Grammar.grammar
    }
}

*/