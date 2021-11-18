package interpreters.common

/**
 * As described in this paper: https://arxiv.org/pdf/2002.09030.pdf
 * This is a property of some program's input/output examples, assuming those examples are of type T
 */
abstract class TypeRefinement<I, O>(val inTypeName : String, val outTypeName : String){
    abstract fun computeProperty(inputs : I, output : O) : Boolean

    enum class PropertySignature {
        TRUE,
        FALSE,
        MIXED,
        NEITHER
    }

    fun computeSignature(beforeState : ProgramState, afterState : ProgramState) : PropertySignature {
        val beforeVars = beforeState.getVars(inTypeName)
        val afterVars = afterState.getVars(outTypeName)
        var trues = 0
        var falses = 0
        if(beforeVars.isEmpty()) {
            // We could not compute this property because we lacked any variables to do so.
            return PropertySignature.NEITHER
        }
        for(it in beforeVars){
            val av = afterVars[it.key] ?: continue
            val prop = computeProperty(it.value as I, av as O)
            if(prop) {
                trues += 1
            }
            else {
                falses += 1
            }
        }
        if(trues > 0 && falses > 0){
            return PropertySignature.MIXED
        }
        else if(trues > 0 && falses == 0) {
            return PropertySignature.TRUE
        }
        else if(falses > 0 && trues == 0) {
            return PropertySignature.FALSE
        }
        else {
            return PropertySignature.NEITHER
        }
    }
}