package generators

import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.TerminalAPR
import grammars.common.TerminalProductionRule
import grammars.common.UnexpandedAPR
import java.util.*
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.*
class ProgramGenerator(val grammar: AttributeGrammar, val numRandomTries : Int = 3, val timeoutMs : Long = 1L) {


    /**
     * Given a list of APRs and a list of constraints we have to uphold,
     * return a map from APRs in that list to new constraints we could substitute them out for.
     */
    fun getConstraintSubstitutions(expansions: List<AttributedProductionRule>, constraints: List<RuleConstraint>): Map<AttributedProductionRule, List<RuleConstraint>> {
        val fittingAttributes = constraints.map { //Make a set of attributes that would satisfy these constraints.
            (it.makeSatisfyingAttribute())
        }
        return expansions.map { apr ->
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
    }

    /**
     * Given a node with no attribute rule associated with it and a set of extra constraints
     *  (extra meaning on top of whatever constraints the generated node has to have)
     *  expand it out until valid.
     *  Returns true if we could expand, false if we couldn't.
     */
    fun expandNode(node: GenericGrammarNode, additionalConstraints : List<RuleConstraint> = listOf()) : Boolean {
        // First, create the program by expanding the start symbol.
        val lhsSymbol = node.lhsSymbol()
        if(lhsSymbol.terminal){
            return true // All done!
        }
        val expansions = grammar.getPossibleExpansions(lhsSymbol)
        var foundSatisfying = false
        var tryCount = 0
        while (!foundSatisfying && (tryCount < numRandomTries || numRandomTries == -1)) {
            tryCount += 1
            val substitutedConstraints = this.getConstraintSubstitutions(expansions, additionalConstraints)
            // For each rule + constraints, see if we can expand every node there.
            substitutedConstraints.forEach { ruleEntry ->
                val attributes = node.attributes()
                val expansion = ruleEntry.key
                val ruleConstraints = grammar.constraints[expansion]?.generate(attributes) ?: listOf()
                val allNewConstraints = ruleEntry.value + ruleConstraints // All the new constraints our expansion would need.
                // Apply it, if we can.
                var expansionIsSatisfying = true
                val childNodes = expansion.rule.rhs.mapIndexed { index, childSymbol ->
                    if(!expansionIsSatisfying){
                        return@forEach
                    }
                    val newChild = GrammarNode(UnexpandedAPR(childSymbol), node, index)
                    if(!childSymbol.terminal) {
                        // If the child needs to be expanded, expand it.
                        if(!expandNode(newChild, allNewConstraints)){
                            expansionIsSatisfying = false
                        }
                    }
                    newChild
                }
                if(expansionIsSatisfying) {
                    // We're done! Hooray! Just add these expanded nodes as our current node's children.
                    node.rhs = childNodes
                    node.productionRule = expansion
                    return true
                }
            }
        }
        return false //
    }

    fun generate(rootConstraints: List<RuleConstraint> = listOf()): RootGrammarNode = runBlocking {
        val firstRule = grammar.getPossibleExpansions(grammar.start).random()
        val program = RootGrammarNode(firstRule)
        try {
            withTimeout(timeMillis = 100L) {
                expandNode(program, rootConstraints)
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