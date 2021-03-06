package subscripts

import languages.deepcoder.DeepcoderLanguage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import languages.*
import languages.haskell.HaskellASTLanguage
import languages.haskell.HaskellASTType
import languages.haskell.HaskellLanguage
import languages.lambda2.Lambda2FunctionalLanguage


suspend fun <A> Iterable<A>.pforall(f: suspend (A) -> Unit) = coroutineScope {
    map {
        async {
            f(it)
            true
        }
    }.awaitAll()
}

val MAX_COROUTINES = Runtime.getRuntime().availableProcessors() - 2;


enum class LanguageRef {
    DEEPCODER,
    LAMBDA2,
    LAMBDA2CFG,
    LAMBDA2CFGSKETCH,
    LAMBDA2NOARITHCFG,
    HASKELLAST,
    HASKELLTYPEAST,
    HASKELL,
}

fun argsToLanguage(lan : LanguageRef) : Language<*, *> {
    when(lan){
        LanguageRef.DEEPCODER -> return DeepcoderLanguage()
        LanguageRef.LAMBDA2 -> return Lambda2FunctionalLanguage()
        LanguageRef.LAMBDA2CFG -> return CfgLanguage(Lambda2FunctionalLanguage())
        LanguageRef.LAMBDA2CFGSKETCH -> return PreprocessedCfgLanguage(CfgLanguage(Lambda2FunctionalLanguage(doSketch = true)))
        LanguageRef.LAMBDA2NOARITHCFG -> return CfgLanguage(Lambda2FunctionalLanguage(excludedFunctions = setOf("plus", "times", "minus")))
        LanguageRef.HASKELLAST -> return HaskellASTLanguage()
        LanguageRef.HASKELLTYPEAST -> return HaskellASTLanguage(HaskellASTType.TYPE)
        LanguageRef.HASKELL -> return HaskellLanguage()
    }
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
        }.toMap()
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
        if(topK != null && topK < pairs.size) {
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

fun <T> Collection<T>.countMap(keySet : Collection<T> = setOf()) : Map<T, Int> {
    val mmap = mutableMapOf<T, Int>()
    keySet.forEach {
        mmap[it] = 0
    }
    this.forEach { it ->
        mmap[it] = (mmap[it] ?: 0) + 1
    }
    return mmap.toMap()
}