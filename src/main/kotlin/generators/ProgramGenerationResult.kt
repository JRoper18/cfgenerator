package generators

import grammar.GenericGrammarNode
import grammar.RootGrammarNode

data class ProgramGenerationResult(val program: RootGrammarNode,
                                   val examples : Collection<Pair<String, String>>,
                                   val status: PROGRAM_STATUS,
                                   val errors : MutableMap<String, MutableList<Exception>> = mutableMapOf()
){
    enum class PROGRAM_STATUS {
        BAD,
        EXCEPTIONED,
        RUNNABLE
    }

}