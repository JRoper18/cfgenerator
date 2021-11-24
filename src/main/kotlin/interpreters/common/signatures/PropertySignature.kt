package interpreters.common.signatures

import interpreters.common.executors.FunctionExecutor
import languages.TypedFunctionalLanguage

/**
 * As described in this paper: https://arxiv.org/pdf/2002.09030.pdf
 * This is a property of some program's input/output examples, assuming those examples are inputted as I and outputted as O
 */
abstract class PropertySignature<I, O>(val inTypeNames : List<String>, val outTypeName : String){
    abstract fun computeProperty(inputs : I, output : O) : Boolean
    companion object {
        const val anyType = FunctionExecutor.anyType
    }
    enum class Result {
        TRUE,
        FALSE,
        MIXED,
    }

    fun computeSignature(examples : Collection<Pair<I, O>>) : Result {
        require(examples.isNotEmpty()) {
            "Cannot compute signature of no examples"
        }
        var trues = 0
        var falses = 0
        for(it in examples){
            val inV = it.first
            val outV = it.second
            val prop = computeProperty(inV, outV)
            if(prop) {
                trues += 1
            }
            else {
                falses += 1
            }
        }
        if(trues == 0) {
            return Result.FALSE
        }
        else if(falses == 0) {
            return Result.TRUE
        }
        return Result.MIXED
    }
}