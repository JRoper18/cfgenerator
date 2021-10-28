package subscripts

import grammars.CfgLanguage
import grammars.ProgramGenerationResult
import grammars.Language
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
    LAMBDA2CFG,
}

fun argsToLanguage(lan : LanguageRef) : Language {
    when(lan){
        LanguageRef.DEEPCODER -> return DeepcoderLanguage()
        LanguageRef.LAMBDA2 -> return Lambda2Language()
        LanguageRef.LAMBDA2CFG -> return CfgLanguage(Lambda2Language())
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

class FrequencyCounter(val counts : Map<String, Int>, 
    val include : Set<String>? = null,
    val topK : Int? = null
) {
    var total : Int = 0
    init {
        counts.forEach {
            total += it.value
        }
    }

    fun freqDiff(other : FrequencyCounter) : FrequencyCounter {
        val otherFreqs = other.freqs()
        val newCounts : Map<String, Int> = freqs().map {
            val itemDiff = (it.value - (otherFreqs[it.key] ?: 0.0f))
            val scaledDiff : Int = (itemDiff * 100000f).toInt()!!
            if(scaledDiff < 0) {
                Pair(it.key, 0)
            }
            else {
                Pair(it.key, scaledDiff)
            }
        }.toMap<String, Int>()
        return FrequencyCounter(newCounts, include, topK)
    }

    fun freqs() : Map<String, Float> {
        return counts.toList().map {
            Pair(it.first, (it.second.toFloat() / total))
        }.toMap()
    }

    override fun toString() : String {
        val build = StringBuilder()
        var pairs = freqs().toList().sortedBy {
            -it.second //Negate to make it high-to-low
        }.filter {
            if(include == null){
                true
            }
            else {
                it.first.trim() in include
            }
        }
        if(topK != null) {
            pairs = pairs.subList(0, topK)
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