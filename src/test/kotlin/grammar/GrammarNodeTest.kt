package grammar

import grammars.common.StatementListProductionRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GrammarNodeTest {

    @Test
    fun testSynthesizedAttributeGeneration() {
        val tree = GrammarNode(APR(StatementListProductionRule)).withChildren{parent -> listOf(
            GrammarNode(APR(PR(NtSym("Stmt"), listOf())), parent),
            GrammarNode(APR(PR(NtSym("Stmt"), listOf())), parent)
        )}
    }
}
