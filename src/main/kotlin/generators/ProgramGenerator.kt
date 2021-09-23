package generators

import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.TerminalProductionRule
import java.util.*
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.*
class ProgramGenerator(val grammar: AttributeGrammar, val numRandomTries : Int = 3, val timeoutMs : Long = 1L) {
    fun generateWithConstraints(constraints : List<RuleConstraint>) : GenericGrammarNode? {
        if(constraints.isEmpty()) {
            return generate()
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
            rulesWithConstraints.forEach { ruleEntry ->
                val expansion = ruleEntry.key
                val newConstraints = ruleEntry.value
                // We have an expansion and any new constraints. Can we make a program with it, though?
                if(newConstraints.isNotEmpty()){
                    // Generate a program
                    val possibleProgram = generateWithConstraints(newConstraints)
                    if(possibleProgram != null){
                        // Pass it in so that the APR can make a program given one that solves it's constraints.
                        val totalProgram = expansion.makeProgramWithAttribute(fittingAttributes[0], possibleProgram)
                        if(totalProgram != null){
                            return totalProgram
                        }
                    }
                }
                else {
                    val newProg = expansion.makeProgramWithAttribute(fittingAttributes[0])
                        ?: return null //TODO Handle more than 1-element list
                    val unexpanded = newProg.getUnexpandedNodes()
                    unexpanded.forEach {
                        expandNode(it)
                    }
                    return newProg
                }
            }
        }
        return null
    }
    fun expandNode(node: GenericGrammarNode, constraints : List<RuleConstraint> = listOf()) {
        // First, create the program by expanding the start symbol.
        val lhsSymbol = node.lhsSymbol()
        if(lhsSymbol.terminal){
            return
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
            if(constraints == null) {
                satisfies = true
            } else {
                satisfies = true
                val attributes = node.attributes()
                val generatedConstraints = constraints.generate(attrs = attributes)
                generatedConstraints.forEach {
                    if(!it.satisfies(attributes)){
                        satisfies = false
                    }
                }
            }
            if(satisfies){
                val newNodes = expansion.rule.rhs.mapIndexed { index, symbol ->
                    GrammarNode(APR(TerminalProductionRule(symbol)), node, index)
                }
                // The new nodes need to be expanded, so add them to the queue.
                node.rhs = newNodes
                node.productionRule = expansion
                newNodes.forEach {
                    expandNode(it, listOf())
                }
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

    fun generate(): RootGrammarNode = runBlocking {
        val firstRule = grammar.getPossibleExpansions(grammar.start).random()
        val program = RootGrammarNode(firstRule)
        try {
            withTimeout(timeMillis = 100L) {
                expandNode(program, listOf())
                program
            }
        } catch (ex: TimeoutException) {
            // We ran out of time.
            println("Ran out of time. Program so far: ")
            println(program)
            println(ProgramStringifier().stringify(program))
            throw ex
        }
        program
    }
}