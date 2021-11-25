package interpreters.common.signatures

class IdentityProperty : PropertySignature<Any, Any>(listOf("any"), "any") {
    override fun computeProperty(inputs: Any, output: Any): Boolean {
        return inputs.equals(output)
    }
}