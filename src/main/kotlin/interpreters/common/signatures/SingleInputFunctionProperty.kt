package interpreters.common.signatures

abstract class SingleInputFunctionProperty<in I, in O>(inType : String, outType : String) : FunctionalPropertySignature(listOf(inType), outType) {
    override fun computeProperty(inputs: List<Any>, output: Any): Boolean {
        require(inputs.size == 1) {
            "Must have a single input to this function!"
        }
        return computeSingleProperty(inputs[0] as I, output as O)
    }
    abstract fun computeSingleProperty(input : I, output : O) : Boolean
}