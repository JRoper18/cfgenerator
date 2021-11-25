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
    fun examplesToString(examples : Collection<Pair<I, O>>) : String{
        val build = StringBuilder()
        build.append("Examples:\n")
        examples.map {
            exampleToString(it)
        }.forEach {
            build.append("Inputs: \n")
            build.append(it.first + "\n")
            build.append("Output: \n")
            build.append(it.second + "\n")
        }
        return build.toString()
    }
    fun generationResultToString(result : ProgramGenerationResult<I, O>) : String {
        val build = StringBuilder()
        build.append(examplesToString(result.examples))
        build.append("\nProgram: \n")
        build.append(this.programToString(result.program) + "\n")
        return build.toString()
    }
}