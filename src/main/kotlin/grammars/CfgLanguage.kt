package grammars

import grammar.AttributeGrammar
import grammar.RootGrammarNode
import grammars.Language

class CfgLanguage(val language: Language, val attrReg : Regex = Regex("retType")) : Language {
    override fun generateProgramAndExamples(numExamples: Int): ProgramGenerationResult {
        return language.generateProgramAndExamples(numExamples)
    }

    override fun isProgramUseful(result: ProgramGenerationResult): Boolean {
        return language.isProgramUseful(result)
    }

    override fun programToString(program: RootGrammarNode): String {
        return grammar().encode(program, attrReg)
    }

    override fun runProgramWithExample(program: String, input: String): String {
        val progs = this.grammar().decode(program)
        require(progs.size == 1) {
            "Decode found multiple programs in the given string!"
        }
        return language.runProgramWithExample(language.programToString(progs[0]), input)
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunResult {
        val progTree : RootGrammarNode
        try {
            val progs = this.grammar().decode(program)
            progTree = progs[0]
        } catch (ex : Exception) {
            return ProgramRunResult.DECODEERROR
        }
        try {
            progTree.verify()
        } catch (ex : Exception) {
            return ProgramRunResult.VERIFYERROR
        }
        return runProgramAgainstExample(language.programToString(progTree), input, output)
    }

    override fun grammar(): AttributeGrammar {
        return language.grammar()
    }
}