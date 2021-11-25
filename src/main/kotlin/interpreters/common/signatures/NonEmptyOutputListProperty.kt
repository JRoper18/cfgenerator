package interpreters.common.signatures


class NonEmptyOutputListProperty(val listType : String) : SingleInputFunctionProperty<Any, List<Any>>(listType, anyType) {
    override fun computeSingleProperty(inputs: Any, output: List<Any>): Boolean {
        return output.isNotEmpty()
    }
}