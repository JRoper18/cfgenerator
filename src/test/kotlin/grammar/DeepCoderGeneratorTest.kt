package grammar

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.constraints.BasicRuleConstraint
import grammars.deepcoder.deepCoderGrammar
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DeepCoderGeneratorTest {

    @Test
    fun generate() {
        val generator = ProgramGenerator(deepCoderGrammar, numRandomTries = 1)
        val program = (generator.generateWithConstraints(listOf(BasicRuleConstraint(NodeAttribute("length", "1")))))
        println(program)
        if(program != null){
            println(ProgramStringifier().stringify(program!!))
        }
    }
}