package subscripts

import grammars.ProgramGenerationResult
import grammars.Language
import grammars.deepcoder.DeepcoderLanguage
import grammars.lambda2.Lambda2CfgLanguage
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
    LAMBDA2CFG,
}

fun argsToLanguage(lan : LanguageRef) : Language {
    when(lan){
        LanguageRef.DEEPCODER -> return DeepcoderLanguage()
        LanguageRef.LAMBDA2 -> return Lambda2Language()
        LanguageRef.LAMBDA2CFG -> return Lambda2CfgLanguage()
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

class FrequencyCounter(val counts : Map<String, Int>, val include : Set<String> = setOf("cons", "foldl", "foldr", "map", "recl", "filter")
) {
    var total : Int = 0
    init {
        counts.forEach {
            total += it.value
        }
    }

    override fun toString() : String {
        val build = StringBuilder()
        val pairs = counts.toList().map {
            Pair(it.first, (it.second.toFloat() / total))
        }.sortedBy {
            -it.second //Negate to make it high-to-low
        }.filter {
            it.first in include
        }
        pairs.forEach {
            build.append(it.first)
            build.append('=')
            build.append("%.4f".format(it.second))
            build.append(" ")
        }
        return build.toString()
    }

}