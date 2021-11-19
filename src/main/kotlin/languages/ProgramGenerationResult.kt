package languages

import grammar.GenericGrammarNode
import grammar.RootGrammarNode
import interpreters.common.signatures.PropertySignature

data class ProgramGenerationResult<I, O>(
    val program: RootGrammarNode,
    val examples : Collection<Pair<I, O>>,
    val status: PROGRAM_STATUS,
    val errors : MutableMap<String, MutableList<Exception>> = mutableMapOf(),
    val properties : Map<GenericGrammarNode, Map<PropertySignature<Any, Any>, PropertySignature.Result>> = mapOf(),
){
    enum class PROGRAM_STATUS {
        BAD,
        EXCEPTIONED,
        RUNNABLE
    }

}