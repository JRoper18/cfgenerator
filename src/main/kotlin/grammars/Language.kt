package grammars

import grammar.AttributeGrammar
import grammar.RootGrammarNode

interface Language {
    fun generateProgramAndExamples(numExamples : Int) : ProgramGenerationResult
    fun isProgramUseful(result : ProgramGenerationResult) : Boolean
    fun programToString(program : RootGrammarNode) : String
    fun runProgramWithExample(program : String, input : String) : String
    fun runProgramAgainstExample(program : String, input : String, output : String) : ProgramRunDetailedResult
    fun grammar() : AttributeGrammar
}