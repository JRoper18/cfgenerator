package interpreters.common.signatures


class NonEmptyOutputListProperty(val listType : String) : OnlyOutputFunctionalSignature<List<Any>>(listType) {
    override fun computeOutputProperty(output: List<Any>): Boolean {
        return output.isNotEmpty()
    }
}