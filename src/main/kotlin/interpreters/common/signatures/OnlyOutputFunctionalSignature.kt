package interpreters.common.signatures

abstract class OnlyOutputFunctionalSignature<O>(outType : String) : FunctionalPropertySignature(listOf(), outType) {
    override fun computeProperty(inputs: List<Any>, output: Any): Boolean {
        return computeOutputProperty(output as O)
    }
    abstract fun computeOutputProperty(output : O) : Boolean
}