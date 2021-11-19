package languages

import grammar.AttributeGrammar
import grammar.GenericGrammarNode
import grammar.RootGrammarNode
import interpreters.common.ProgramState
import interpreters.common.signatures.PropertySignature

interface Language<I, O> {
    fun generateProgramAndExamples(numExamples : Int) : ProgramGenerationResult<I, O>
    fun exampleToString(example : Pair<I, O>) : Pair<String, String>
    fun isProgramUseful(result : ProgramGenerationResult<I, O>) : Boolean
    fun programToString(program : RootGrammarNode) : String
    fun runProgramWithExample(program : String, input : String) : String
    fun runProgramAgainstExample(program : String, input : String, output : String) : ProgramRunDetailedResult
    fun grammar() : AttributeGrammar
}