package grammars.lambda2

import grammars.ProgramGenerationResult
import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.AttributeGrammar
import grammar.RootGrammarNode
import grammars.Language
import kotlin.random.Random

class Lambda2CfgLanguage : Lambda2Language() {
    override fun generateProgramAndExamples(numExamples: Int): ProgramGenerationResult {
        val prog = generator.generate()
        val progStr = strfier.stringify(prog)
        val examples = interp.makeExamples(progStr, numExamples)
        val status = if(examples.isEmpty()) ProgramGenerationResult.PROGRAM_STATUS.BAD else ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE
        return ProgramGenerationResult(prog, examples, status)
    }

    override fun programToString(program: RootGrammarNode): String {
        return grammar().encode(program, Regex("retType"))
    }

    override fun runProgramWithExample(program: String, input: String): String {
        val progs = this.grammar().decode(program)
        require(progs.size == 1) {
            "Decode found multiple prorgrams in the given string!"
        }
        return interp.interp(strfier.stringify(progs[0]), input)
    }
}