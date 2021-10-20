package grammars.lambda2

import generators.ProgramGenerationResult
import generators.ProgramGenerator
import generators.ProgramStringifier
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
        return ProgramGenerationResult(prog, examples, ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE)
    }

    override fun isProgramUseful(result: ProgramGenerationResult): Boolean {
        return result.examples.isNotEmpty() && result.status == ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE
    }

    override fun programToString(program: RootGrammarNode): String {
        return strfier.stringify(program)
    }
}