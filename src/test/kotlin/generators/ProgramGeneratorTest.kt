package generators


import grammar.*
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammars.common.InitAttributeProductionRule
import grammars.common.SizedListAttributeProductionRule
import grammars.common.SynthesizeAttributeProductionRule
import grammars.common.TerminalProductionRule
import grammars.deepcoder.DeepCoderGrammar
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random

internal class ProgramGeneratorTest {


    @Test
    fun testExpandToTerminal() {
        val ntSym = NtSym("NT")
        val unitSym = StringSymbol("UNIT")
        val grammar = AttributeGrammar(listOf(APR(PR(ntSym, listOf(unitSym)))
        ), start = ntSym, constraints = mapOf())
        val generator = ProgramGenerator(grammar)
        val program = generator.generate()
        println(program)
        program.verify()
    }
    @Test
    fun testExpandNodeList() {
        val listSym = NtSym("LIST")
        val unitSym = StringSymbol("UNIT")
        val rule = SizedListAttributeProductionRule(listSym, unitSym, " ")
        val grammar = AttributeGrammar(listOf(rule,
            InitAttributeProductionRule(TerminalProductionRule(listSym), "length", "0"),
        ), start = listSym, constraints = mapOf())
        val generator = ProgramGenerator(grammar)

        repeat(5) {
            val cons1 = listOf(BasicRuleConstraint(NodeAttribute("length", it.toString())))
            val program1 = generator.generate(cons1)
            program1.verify() // Will throw exception if the program is wrong.
            assertEquals(it.toString(), program1.attributes().getStringAttribute("length"))

        }
    }

    @Test
    fun testExpandStringsetSymbol() {
        val ntSym = NtSym("NT")
        val setSymbol = StringsetSymbol(setOf("a", "b", "c"))
        val grammar = AttributeGrammar(listOf(SynthesizeAttributeProductionRule(mapOf(setSymbol.attributeName to 0),
            PR(ntSym, listOf(setSymbol)))), start=ntSym, constraints = mapOf())
        val generator = ProgramGenerator(grammar)
        val program = generator.generate()
        val attrs = program.attributes()
        println(attrs)
        println(program.toString(printAPR = true))

        assertNotNull(attrs.getStringAttribute(setSymbol.attributeName))
    }

    @Test
    fun testDeterminism() {
        // Just use deepcoder, it's simple but not so simple that it's inherently deterministic.
        val gen1 = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1, random = Random(100))
        val gen2 = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1, random = Random(100))
        val stringifier = ProgramStringifier()
        repeat(3) {
            assertEquals(stringifier.stringify(gen1.generate(listOf())), stringifier.stringify(gen2.generate(listOf())))
        }
    }

    @Test
    fun testRandomness() {
        val gen1 = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
        val gen2 = ProgramGenerator(DeepCoderGrammar.grammar, numRandomTries = 1)
        val stringifier = ProgramStringifier()
        repeat(3) {
            assertNotEquals(stringifier.stringify(gen1.generate(listOf())), stringifier.stringify(gen2.generate(listOf())))
        }

    }

    @Test
    fun testExpandWithConstraint() {
        val r1 = InitAttributeProductionRule(PR(NtSym("start"), listOf(StringSymbol("a"))), "attr", "a")
        val r2 = InitAttributeProductionRule(PR(NtSym("start"), listOf(StringSymbol("b"))), "attr", "b")
        val gram1 = AttributeGrammar(listOf(
            r1, r2
        ), start=NtSym("start"), constraints = mapOf(
            r1.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("attr", "b")))) //Impossible
        ))

        val gen = ProgramGenerator(gram1, numRandomTries = 1)

        repeat(20) {
            val prog = gen.generate()
            assertEquals("b", prog.attributes().getStringAttribute("attr"))
        }
    }
}