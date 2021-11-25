package interpreters.common.signatures

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PropertySignatureTest {

    @Test
    fun testTyping() {
        val sig : FunctionalPropertySignature = NonEmptyInputListProperty("list")
        sig.computeProperty(listOf(listOf(1)), 0)

    }
}