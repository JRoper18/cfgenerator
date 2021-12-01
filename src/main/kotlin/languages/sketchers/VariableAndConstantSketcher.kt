package languages.sketchers

import languages.Language
import languages.TypedFunctionalLanguage
import utils.combinations
import utils.combinationsLazy

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
    private fun testFill(expandedFill : List<List<String>>, tokens : List<String>, holes : List<Int>, examples : Collection<Pair<String, String>>) : Boolean {
        val filledProg = fill(tokens, holes, expandedFill)
        val filledProgStr = language.detokenize(filledProg)
        var success = true
        for(example in examples) {
            val res = language.runProgramAgainstExample(filledProgStr, example.first, example.second)
            if(!res.result.isGood()) {
                success = false
                break
            }
        }
        return success
    }
    override fun makeFills(
        tokens: List<String>,
        holes: List<Int>,
        examples: Collection<Pair<String, String>>
    ): List<List<String>>? {
        // Try the program without any holes first: Maybe it's good enough at the start.
        val identityFill = holes.map {
            listOf(tokens[it])
        }
        if(testFill(identityFill, tokens, holes, examples)) {
            return identityFill
        }
        val possibleFills = stringset.combinationsLazy(holes.size)
        for(fill in possibleFills) {
            val expandedFill = fill.map {
                listOf(it)
            }
            if(testFill(expandedFill, tokens, holes, examples)){
                return expandedFill
            }
        }
        return null
    }

}