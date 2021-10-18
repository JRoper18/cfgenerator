package generators

import grammar.RootGrammarNode
import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.TerminalAPR
import grammars.common.UnexpandedAPR
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.*
import kotlin.random.Random

class ProgramGenerator(val ag: AttributeGrammar,
                       val numRandomTries : Int = 3,
                       val timeoutMs : Long = 10000L,
                       val maxProgramDepth : Int = 10,
                       val random: Random = Random(42L)
) {


    /**
     * Given a list of our current attributes, APRs, and a list of attributes we want to make,
     * return a map from APRs in that list to new constraints we could substitute them out for.
     */
    fun getConstraintSubstitutions(nodeAttributes: NodeAttributes, expansions: List<AttributedProductionRule>, additionalConstraints: List<RuleConstraint>): Map<AttributedProductionRule, List<List<RuleConstraint>>> {
        val agExpansionConstraints = expansions.map {
            Pair(it,ag.constraints[it.rule]?.generate(attrs = nodeAttributes, random = this.random) ?: listOf())
        }
        return agExpansionConstraints.map { entry ->
            val apr = entry.first
            val constraints = entry.second + (additionalConstraints)
            var canMakeProgramTrade : Pair<Boolean, List<List<RuleConstraint>>>
            if(constraints.isEmpty()) {
                // No constraints!
                canMakeProgramTrade = Pair(true, listOf())
            }
            else {
                val fittingAttributes = constraints.map {
                    (it.makeSatisfyingAttribute())
                } //Make a set of attributes that would satisfy these constraints.
                canMakeProgramTrade = apr.canMakeProgramWithAttributes(NodeAttributes.fromList(fittingAttributes))
            }
            Pair(apr, canMakeProgramTrade)
        }.filter { aprPair ->
            aprPair.second.first
        }.map {
            Pair(it.first, it.second.second)
        }.groupBy {
            it.first
        }.mapValues { value ->// Finally, just remove the extra attributes.
            val noExtra = value.value[0].second // We take the zeroth because we assume that the list created by groupBy is size 1,
            // Because we don't have APRs that map to more than one list of constraint lists.
            require(value.value.size == 1) {
                "APR ${value.key} is somehow leading to multiple sets of constraints. "
            }
            noExtra
        }
        // And now we have a map from attributes to ALL their required constraints, only if they can actually be made (according to their functions).

    }

    /**
     * Given a node with no attribute rule associated with it and a set of extra constraints
     *  (extra meaning on top of whatever constraints the generated node has to have)
     *  expand it out until valid.
     *  Returns true if we could expand, false if we couldn't.
     */
    fun expandNode(node: GenericGrammarNode, additionalConstraints : List<RuleConstraint> = listOf(), depth : Int = 0) : Boolean {
        // First, create the program by expanding the start symbol.
        if(depth > maxProgramDepth) {
            return false
        }
        val lhsSymbol = node.lhsSymbol()
        val attributes = node.attributes()
        if(node.lhsSymbol().terminal){
            // If the node's is terminal then just return if it satisfies the constraints.
            val fillsCons = additionalConstraints.find {
                !it.satisfies(attributes)
            } == null // Return the first constraint that doesn't satisfy, null if they all satisfy.
            if(fillsCons) {
                node.productionRule = TerminalAPR(node.lhsSymbol())
            }
            return fillsCons
        }
        val expansions = ag.getPossibleExpansions(lhsSymbol)
        var foundSatisfying = false
        var tryCount = 0
        while (!foundSatisfying && (tryCount < numRandomTries || numRandomTries == -1)) {
            tryCount += 1

            val substitutedConstraints = this.getConstraintSubstitutions(attributes, expansions, additionalConstraints)
            // For each rule + constraints, see if we can expand every node there.
            for(ruleEntry in substitutedConstraints.toList().shuffled(random)) {
                val expansion = ruleEntry.first
                val allNewConstraints = ruleEntry.second
                //For our new set of constraints, generate a new set of satisfying attributes.
                val allNewFittingAttributes = allNewConstraints.map { consList ->
                    consList.map {
                        it.makeSatisfyingAttribute(random)
                    }
                }
                var newChildren : List<GenericGrammarNode>
                var expansionIsGood = true
                if(allNewConstraints.isEmpty()){
                    //We have no constraints. Generate anything we want.
                    newChildren = expansion.makeChildren()
                }
                else {
                    val satisfyingSubprograms = mutableListOf<GenericGrammarNode>()
                    for(i in 0..allNewFittingAttributes.size-1) {
                        val fittingAttributesForThisChild = allNewFittingAttributes[i]
                        val allNewConstraintsForThisChild = allNewConstraints[i]
                        var satisfyingSubprogram : RootGrammarNode? = null
                        var foundSatisfyingSubprogram = false
                        // For each symbol in our grammar, find one that gives us a satisfying subprogram.
                        val symbol = expansion.rule.rhs[i]
                        if(symbol.terminal) {
                            // Can't go farther down from here.
                        }
                        else {
                            satisfyingSubprogram = RootGrammarNode(UnexpandedAPR(symbol))
                            foundSatisfyingSubprogram = expandNode(satisfyingSubprogram, allNewConstraintsForThisChild, depth + 1) //Make a satisfying subprogram for the make...() call.
                            if(foundSatisfyingSubprogram) {
                                satisfyingSubprograms.add(satisfyingSubprogram)
                            }
                            else {
                                break;
                            }

                        }
                    }
                    if(satisfyingSubprograms.size != allNewFittingAttributes.size) {
                        // We couldn't create satisfying subprograms.
                        continue
                    }
                    newChildren = expansion.makeChildrenForAttributes(NodeAttributes.fromList(allNewFittingAttributes.flatten()), nodesThatFit = satisfyingSubprograms)
                }
                //Now, just expand the children trees.
                node.withExpansionTemporary(expansion, newChildren, {
                    for(child in it.rhs){
                        val allUnexpanded = child.getUnexpandedNodes()
                        for(unexpanded in allUnexpanded) {
                            val canExpand = expandNode(unexpanded, listOf())
                            if(!canExpand){
                                expansionIsGood = false
                                break
                            }
                        }
                        if(!expansionIsGood) {
                            break
                        }
                    }
                }, {
                    expansionIsGood
                })
                if(expansionIsGood) {
                    return true
                }
            }
        }
        return false
    }

    fun generate(rootConstraints: List<RuleConstraint> = listOf()): RootGrammarNode = runBlocking {
        var program = RootGrammarNode(UnexpandedAPR(ag.start))
        try {
            withTimeout(timeMillis = timeoutMs) {
                expandNode(program, rootConstraints)
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