package generators

import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.TerminalProductionRule
import java.util.*
import java.util.concurrent.TimeoutException

class ProgramGenerator(val numRandomTries : Int = 3) {
    fun generateWithConstraints(grammar: AttributeGrammar, constraints : List<RuleConstraint>) : GenericGrammarNode? {
        if(constraints.isEmpty()) {
            return generate(grammar)
        }
        var tryCount = 0
        while ((tryCount < numRandomTries || numRandomTries == -1)) {
            tryCount += 1

            val fittingAttributes = constraints.map {
                (it.makeSatisfyingAttribute())
            }

            val rulesWithConstraints = grammar.rules.map { apr ->
                val satisfiesEachConstraint = constraints.mapIndexed { index, ruleConstraint ->
                    val canMakeData = apr.canMakeProgramWithAttribute(fittingAttributes[index])
                    canMakeData
                }
                Pair(apr, satisfiesEachConstraint)
            }.filter { aprPair ->
                val fittingConstraints = aprPair.second.filter {
                    it.first
                }
                fittingConstraints.size == aprPair.second.size // If the rule can't handle some constraint, then remove it from the list entirely.
            }.map {
                Pair(it.first, it.second.flatMap {
                    it.second
                }) // Map to remove the booleans and flatmap the rule constraints together
            }.groupBy {
                it.first
            }.mapValues { value ->// Finally, just remove the extra attributes.
                value.value.flatMap {
                    it.second
                }
            } // And now we have a map from attributes to ALL their required constraints, only if they can actually be made (according to their functions).

            val expansion = rulesWithConstraints.keys.random()
            val newConstraints = rulesWithConstraints[expansion]!!
            // We have an expansion and any new constraints. Can we make a program with it, though?
            if(newConstraints.isNotEmpty()){
                // Generate a program
                val possibleProgram = generateWithConstraints(grammar, newConstraints)
                if(possibleProgram == null){
                    // Couldn't do it.
                    continue
                }
                // Pass it in so that the APR can make a program given one that solves it's constraints.
                val totalProgram = expansion.makeProgramWithAttribute(fittingAttributes[0], possibleProgram)
                if(totalProgram == null){
                    continue
                }
                return totalProgram
            }
        }
        return null
    }
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
            val expansions = grammar.getPossibleExpansions(lhsSymbol)

            var foundSatisfying = false
            var tryCount = 0
            while (!foundSatisfying && (tryCount < numRandomTries || numRandomTries == -1)) {
                // Choose an expansion at random.
                val expansion = expansions.random()
                val constraints = grammar.constraints[expansion]
                // Apply it, if we can.
                var satisfies = false
                if((constraints ?: listOf<RuleConstraint>()).isEmpty()) {
                    satisfies = true
                } else {
                    satisfies = true
                    val attributes = toExpand.attributes()
                    constraints!!.forEach {
                        if(!it.satisfies(attributes)){
                            satisfies = false
                        }
                    }
                }
                if(satisfies){
                    val newNodes = expansion.rule.rhs.mapIndexed { index, symbol ->
                        GrammarNode(APR(TerminalProductionRule(symbol)), toExpand, index)
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