package languages

import grammar.RootGrammarNode

data class ProgramGenerationResult<I, O>(val program: RootGrammarNode,
                                   val examples : Collection<Pair<I, O>>,
                                   val status: PROGRAM_STATUS,
                                   val errors : MutableMap<String, MutableList<Exception>> = mutableMapOf()
){
    enum class PROGRAM_STATUS {
        BAD,
        EXCEPTIONED,
        RUNNABLE
    }

}