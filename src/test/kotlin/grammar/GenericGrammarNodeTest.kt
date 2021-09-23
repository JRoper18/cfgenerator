package grammar

import grammars.common.TerminalAPR
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class GenericGrammarNodeTest {

    @Test
    fun testGetUnexpandedNodes() {
        val ntsym = NtSym("NtSym")
        val tsym = StringSymbol("Terminal")
        val rule = APR(PR(ntsym, listOf(tsym)))
        val prog = RootGrammarNode(rule)
        assert(prog.getUnexpandedNodes().isNotEmpty())
        print(prog.getUnexpandedNodes())
        assertEquals(prog.getUnexpandedNodes()[0].lhsSymbol(), ntsym)
        prog.rhs = listOf(GrammarNode(
            TerminalAPR(tsym), prog, 0))
        assert(prog.getUnexpandedNodes().isEmpty())
    }
}