package interpreters.common.signatures

class IsInputDivisibleSignature(intType : String, val div : Int) : SingleInputFunctionProperty<Int, Any>(intType, anyType) {
    override fun computeSingleProperty(input: Int, output: Any): Boolean {
        return input % div == 0
    }
}