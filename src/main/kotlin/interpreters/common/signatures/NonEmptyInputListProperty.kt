package interpreters.common.signatures


class NonEmptyInputListProperty(val listType : String) : SingleInputFunctionProperty<List<Any>, Any>(listType, anyType) {
    override fun computeSingleProperty(input: List<Any>, output: Any): Boolean {
        return input.isNotEmpty()
    }
}