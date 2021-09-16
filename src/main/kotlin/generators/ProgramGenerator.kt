package generators

import grammar.*
import grammars.common.TerminalProductionRule
import java.util.*
import java.util.concurrent.TimeoutException

class ProgramGenerator(val numRandomTries : Int = 3) {
    fun generate(grammar: AttributeGrammar): RootGrammarNode {
        // First, create the program by expanding the start symbol.
        val startRules = grammar.getPossibleExpansions(grammar.start)
        val startExpand = startRules.random()
        val program = RootGrammarNode(startExpand)
        val expandQueue : Queue<GenericGrammarNode> = LinkedList(listOf(program))
        while(expandQueue.isNotEmpty()) {
            val toExpand = expandQueue.poll()
            val lhsSymbol = toExpand.lhsSymbol()
            if(lhsSymbol.terminal){
                continue
            }
            println(program)
            val expansions = grammar.getPossibleExpansions(lhsSymbol)
            // Choose an expansion at random.
            val expansion = expansions.random()
            // Apply it, if we can.
            var foundSatisfying = false
            var tryCount = 0
            while (!foundSatisfying && (tryCount < numRandomTries || numRandomTries == -1)) {
                if(expansion.satisfiesConstraints(toExpand.attributes())){
                    val newNodes = expansion.rule.rhs.mapIndexed { index, symbol ->
                        GrammarNode(TerminalProductionRule(symbol), toExpand, index)
                    }
                    // The new nodes need to be expanded, so add them to the queue.
                    toExpand.rhs = newNodes
                    toExpand.productionRule = expansion
                    expandQueue.addAll(newNodes)
                    foundSatisfying = true
                    tryCount += 1
                }
            }
            if(!foundSatisfying) {
                // In the future we'll try and edit the rest of the tree to make the attributes fit, but this is non-trivial.
                // For now, just give up.
                throw TimeoutException("Give up fool")
            }
        }
        return program
    }
}