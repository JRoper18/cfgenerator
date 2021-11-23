package interpreters.common.signatures


class NonEmptyOutputListProperty(val listType : String) : PropertySignature<Any, List<Any>>(listOf(anyType), listType) {
    override fun computeProperty(inputs: Any, output: List<Any>): Boolean {
        return output.isNotEmpty()
    }
}