package grammars

import generators.ProgramGenerationResult
import grammar.RootGrammarNode

interface Language {
    fun generateProgramAndExamples(numExamples : Int) : ProgramGenerationResult
    fun isProgramUseful(result : ProgramGenerationResult) : Boolean
    fun programToString(program : RootGrammarNode) : String
}