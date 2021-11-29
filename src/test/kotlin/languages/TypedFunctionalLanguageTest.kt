package languages

import languages.lambda2.Lambda2FunctionalLanguage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TypedFunctionalLanguageTest {

    @Test
    fun testMakeNewVariableNode() {
        val varnames = listOf("a", "b", "c", "r", "q")
        varnames.forEach {
            Lambda2FunctionalLanguage().makeNewVariableNode(it).verify()
        }
    }

    @Test
    fun testMakeUnexpandedStmtNode() {
        val varnames = listOf("a", "b")
        val lan = Lambda2FunctionalLanguage()
        val res = lan.makeUnexpandedLambda(varnames, "equals")
        res.second.forEach {
            val expandSuccess = lan.generator.expandNode(it)
            assertTrue(expandSuccess)
        }
        res.first.verify()
    }
}