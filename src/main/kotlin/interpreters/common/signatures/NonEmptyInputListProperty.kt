package interpreters.common.signatures


class NonEmptyInputListProperty(val listType : String) : PropertySignature<List<Any>, Any>(listType, anyType) {
    override fun computeProperty(inputs: List<Any>, output: Any): Boolean {
        return inputs.isNotEmpty()
    }
}