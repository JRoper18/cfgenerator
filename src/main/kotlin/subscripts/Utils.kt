package subscripts

import generators.ProgramGenerationResult
import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.RootGrammarNode
import grammar.constraints.BasicRuleConstraint
import grammars.Language
import grammars.deepcoder.DeepCoderGrammar.FUNCTION_NAME
import grammars.deepcoder.DeepCoderInterpreter
import grammars.deepcoder.DeepCoderVariables
import grammars.deepcoder.DeepcoderLanguage
import grammars.lambda2.Lambda2Language
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


suspend fun <A> Iterable<A>.pforall(f: suspend (A) -> Unit) = coroutineScope {
    map {
        async {
            f(it)
            true
        }
    }.awaitAll()
}

const val MAX_COROUTINES = 100;


enum class LanguageRef {
    DEEPCODER,
    LAMBDA2,
}

fun argsToLanguage(lan : LanguageRef) : Language {
    when(lan){
        LanguageRef.DEEPCODER -> return DeepcoderLanguage()
        LanguageRef.LAMBDA2 -> return Lambda2Language()
    }
}
fun generationResultToString(language : Language, result: ProgramGenerationResult) : String {
    val build = StringBuilder()
    build.append("Examples:\n")
    result.examples.forEach {
        build.append("Inputs: \n")
        build.append(it.first + "\n")
        build.append("Output: \n")
        build.append(it.second + "\n")
    }
    build.append("\nProgram: \n")
    build.append(language.programToString(result.program) + "\n")
    return build.toString()
}