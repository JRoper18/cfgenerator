package languages

import grammar.AttributeGrammar
import grammar.RootGrammarNode

// By default, print all attributes. 
class CfgLanguage<I, O>(val language: Language<I, O>, val attrReg : Regex = Regex("(.*?)")) : Language<I, O> {
    override fun generateProgramAndExamples(numExamples: Int): ProgramGenerationResult<I, O>{
        return language.generateProgramAndExamples(numExamples)
    }

    override fun exampleToString(example: Pair<I, O>): Pair<String, String> {
        return language.exampleToString(example)
    }

    override fun isProgramUseful(result: ProgramGenerationResult<I, O>): Boolean {
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


    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        val progTree : RootGrammarNode
        try {
            val progs = this.grammar().decode(program)
            progTree = progs[0]
        } catch (ex : Exception) {
            return ProgramRunDetailedResult(ProgramRunResult.DECODEERROR, ex.localizedMessage)
        }
        try {
            progTree.verify()
        } catch (ex : Exception) {
            return ProgramRunDetailedResult(ProgramRunResult.VERIFYERROR, ex.localizedMessage)
        }
        return language.runProgramAgainstExample(language.programToString(progTree), input, output)
    }

    override fun grammar(): AttributeGrammar {
        return language.grammar()
    }
}