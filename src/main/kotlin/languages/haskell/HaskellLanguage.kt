package languages.haskell

import generators.GenerationConfig
import grammar.AttributeGrammar
import grammar.RootGrammarNode
import interpreters.haskell.HaskellInterpreter
import languages.Language
import languages.ProgramGenerationResult
import languages.ProgramRunDetailedResult
import languages.ProgramRunResult

class HaskellLanguage : Language<String, String> {
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

    private fun buildTotalProgram(program : String) : String {
        val totalProg = "module Main where\n${program}"
        return totalProg
    }
    override fun runProgramWithExample(program: String, input: String): String {
        val totalProg = buildTotalProgram(program) + "\nmain = print $ f ${input.trim()}"
        val actualOut = interp.runHsScript(totalProg)
        return actualOut
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        try {
            val prettyProg = buildTotalProgram(program)
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