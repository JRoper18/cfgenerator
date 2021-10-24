package grammar

import generators.ProgramGenerator
import grammars.common.TerminalAPR
import grammars.common.UnexpandedAPR
import grammars.lambda2.Lambda2Grammar
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

    @Test
    fun testTemporaryChildren() {
        val prog = RootGrammarNode(APR(PR(ntsym, listOf(ntsym2))))
        val expan = APR(PR(ntsym2, listOf(tsym)))
        val terminalProg = RootGrammarNode(UnexpandedAPR(ntsym2))
        terminalProg.withExpansionTemporary(expan, listOf(GrammarNode(TerminalAPR(tsym), prog, 0)), {
            assertEquals(it.rhs[0].parent, terminalProg)
            it.verify()
        })
        assert(terminalProg.rhs.isEmpty())
    }

    @Test
    fun testEncodeDecode() {
        val gen = ProgramGenerator(Lambda2Grammar.grammar)
        repeat(100) {
            val orig = gen.generate(listOf())
            val encoded = orig.encode()
            val remade = Lambda2Grammar.grammar.decode(encoded)[0]
            remade.verify()
            assertEquals(orig.toString(), remade.toString())

        }
    }
}