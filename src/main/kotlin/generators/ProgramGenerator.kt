package generators

import grammar.AttributeGrammar
import grammar.GenericGrammarNode
import grammar.GrammarNode
import grammar.RootGrammarNode
import java.util.*

class ProgramGenerator {
    fun generate(grammar: AttributeGrammar): String {
        // First, create the program by expanding the start symbol.
        val startRules = grammar.getPossibleExpansions(grammar.start)
        val startExpand = startRules.random()
        val program = RootGrammarNode(startExpand)
        val expandQueue : Queue<GenericGrammarNode> = LinkedList(listOf(program))
        while(expandQueue.isNotEmpty()) {
            val toExpand = expandQueue.poll()
            val expansions = grammar.getPossibleExpansions(toExpand.lhsSymbol)
            // Choose an expansion at random.
            val expansion = expansions.random()
            // Apply it, if we can.
            if (expansion.satisfiesConstraints(toExpand.attributes)) {
                val newNodes = expansion.rule.rhs.mapIndexed { index, symbol ->
                    GrammarNode(null, toExpand, index)
                }
            }
            else {

            }
        }
    }
}