package languages.sketchers

import languages.Language
import languages.TypedFunctionalLanguage
import utils.combinations

class VariableAndConstantSketcher(val language : TypedFunctionalLanguage, val stringset : Set<String>) : SimpleHoleAndSketcher {
    override fun punchHoles(tokens: List<String>): List<Int> {
        val idxs = mutableListOf<Int>()
        for(idx in tokens.indices) {
            if(tokens[idx] in stringset) {
                idxs.add(idx)
            }
        }
        return idxs
    }

    override fun makeFills(
        tokens: List<String>,
        holes: List<Int>,
        examples: Collection<Pair<String, String>>
    ): List<List<String>>? {
        val possibleFills = stringset.combinations(holes.size).toList()
        for(fill in possibleFills) {
            val expandedFill = fill.map {
                listOf(it)
            }
            val filledProg = fill(tokens, holes, expandedFill)
            val filledProgStr = language.detokenize(filledProg)
            var failedOnExample = false
            for(example in examples) {
                val res = language.runProgramAgainstExample(filledProgStr, example.first, example.second)
                if(!res.result.isGood()) {
                    failedOnExample = true
                    break
                }
            }
            if(!failedOnExample) {
                return expandedFill
            }
        }
        return null
    }

}