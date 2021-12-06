package languages

import grammar.*
import interpreters.common.ProgramState
import interpreters.common.signatures.PropertySignature

class PreprocessedCfgLanguage<I, O>(val language: CfgLanguage<I, O>) : Language<I, O> {
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
        return language.language.programToString(program)
    }

    override fun runProgramWithExample(program: String, input: String): String {
        return language.language.runProgramWithExample(program, input)
    }


    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        return language.language.runProgramAgainstExample(program, input, output)
    }

    override fun grammar(): AttributeGrammar {
        return language.grammar()
    }

    override fun generationResultToString(result : ProgramGenerationResult<I, O>) : String {
        return language.language.generationResultToString(result)
    }

    override fun bareMinimumPreprocessing(program: String, examples: Collection<Pair<String, String>>): String {
        val progTree : RootGrammarNode
        try {
            val progs = this.grammar().decode(program)
            progTree = progs[0]
        } catch (ex : Exception) {
            // Probably a decode error, in which case...
            return "PREPROCESS ERROR on prog $program:\n ${ex.stackTraceToString()}"
        }
        return language.language.programToString(progTree)
    }

    override fun symbolsToAnalyse() : Set<String> {
        return language.language.symbolsToAnalyse()
    }

    override suspend fun preprocessOnExamples(program: String, examples: Collection<Pair<String, String>>) : String {
        val progStr = bareMinimumPreprocessing(program, examples)
        return language.language.preprocessOnExamples(progStr, examples)
    }
}