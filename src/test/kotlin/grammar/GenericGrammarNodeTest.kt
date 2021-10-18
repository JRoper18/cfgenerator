package grammar

import grammars.common.TerminalAPR
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class GenericGrammarNodeTest {
    val ntsym = NtSym("NtSym")
    val ntsym2 = NtSym("NtSym2")
    val tsym = StringSymbol("Terminal")

    @Test
    fun testGetUnexpandedNodes() {
        val rule = APR(PR(ntsym, listOf(tsym)))
        val prog = RootGrammarNode(rule)
        assert(prog.getUnexpandedNodes().isNotEmpty())
        print(prog.getUnexpandedNodes())
        assertEquals(prog.getUnexpandedNodes()[0].lhsSymbol(), ntsym)
        prog.withChildren(listOf(GrammarNode(
            TerminalAPR(tsym), prog, 0)))
        assert(prog.getUnexpandedNodes().isEmpty())
    }

    @Test
    fun testChildParentReferences() {
        val prog = RootGrammarNode(APR(PR(ntsym, listOf(ntsym2))))
        val terminalProg = RootGrammarNode(APR(PR(ntsym2, listOf(tsym))))
        terminalProg.withChildren(listOf(GrammarNode(TerminalAPR(tsym), prog, 0)))
        val totalProg = prog.withChildren(listOf(terminalProg))
        totalProg.verify()
    }
}