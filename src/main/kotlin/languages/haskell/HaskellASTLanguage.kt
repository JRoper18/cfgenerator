package languages.haskell

import generators.GenerationConfig
import grammar.AttributeGrammar
import grammar.RootGrammarNode
import interpreters.haskell.HaskellInterpreter
import languages.Language
import languages.ProgramGenerationResult
import languages.ProgramRunDetailedResult
import languages.ProgramRunResult


class HaskellASTLanguage(val astType : HaskellASTType = HaskellASTType.PARSE) : Language<String, String> {
    override fun generateProgramAndExamples(
        numExamples: Int,
        config: GenerationConfig
    ): ProgramGenerationResult<String, String> {
        TODO("Not yet implemented")
    }

    val interp = HaskellInterpreter()

    override fun exampleToString(example: Pair<String, String>): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override fun isProgramUseful(result: ProgramGenerationResult<String, String>): Boolean {
        TODO("Not yet implemented")
    }

    override fun programToString(program: RootGrammarNode): String {
        TODO("Not yet implemented")
    }

    private fun buildProgramFromAST(program : String) : String {
        val parsedProg = interp.astToScript(program, astType = this.astType)
        val totalProg = "module Main where\n${parsedProg}"
        return totalProg
    }
    private fun buildTotalProgramWithInput(program : String, input : String) : String {
        return buildProgramFromAST(program) + "\nmain = print $ f ${input.trim()}"
    }
    override fun runProgramWithExample(program: String, input: String): String {
        val totalProg = buildTotalProgramWithInput(program, input)
        val actualOut = interp.runHsScript(totalProg)
        return actualOut
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        try {
            val prettyProg = buildTotalProgramWithInput(program, input)
            try {
                return ProgramRunDetailedResult.fromInputOutput(input, runProgramWithExample(program, input), output)
            } catch (ex : HaskellInterpreter.InterpretError) {
                val detailedMsg = ex.serr + "\nIn Pretty-Program:\n${prettyProg}"
                val rrs : ProgramRunResult = interp.errToRunResult(ex);
                return ProgramRunDetailedResult(rrs, detailedMsg)
            }
        } catch (ex : HaskellInterpreter.InterpretError) {
            return ProgramRunDetailedResult(ProgramRunResult.DECODEERROR, ex.serr + "\nIn AST-Program:\n${program}")
        }
    }

    override fun grammar(): AttributeGrammar {
        TODO("Not yet implemented")
    }

    override suspend fun preprocessOnExamples(program: String, examples: Collection<Pair<String, String>>): String {
        return program
    }
}