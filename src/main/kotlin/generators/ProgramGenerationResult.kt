package generators

import grammar.RootGrammarNode

data class ProgramGenerationResult(val program: RootGrammarNode, val examples : Collection<Pair<String, String>>, val status: PROGRAM_STATUS){
    enum class PROGRAM_STATUS {
        BAD,
        EXCEPTIONED,
        RUNNABLE
    }

}