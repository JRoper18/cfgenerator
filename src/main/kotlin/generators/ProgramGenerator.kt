package generators

import grammar.RootGrammarNode
import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.rules.TerminalAPR
import grammars.common.rules.UnexpandedAPR
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.*
import kotlin.random.Random

class ProgramGenerator(val ag: AttributeGrammar,
                       val random: Random = Random,
                       val timeoutMs : Long = 999999999999L,
                       val returnPartialOnTimeout : Boolean = false,
) {

    private val defaultGenerationConfig : GenerationConfig = GenerationConfig(ag)
    private fun isValidAttributeList(attrList : List<NodeAttribute>) : Boolean {
        // No duplicate keys.
        return attrList.distinctBy {
            it.first
        }.size == attrList.size
    }
    /**
     * Given a list of our current attributes, APRs, and a list of attributes we want to make,
     * return a map from APRs in that list to new constraints we could substitute them out for.
     */
    fun getConstraintSubstitutions(nodeToSubstitute: GenericGrammarNode, expansions: List<AttributedProductionRule>, additionalConstraints: List<RuleConstraint>): Map<AttributedProductionRule, List<List<RuleConstraint>>> {
        val agExpansionConstraints = expansions.map { apr ->
            var pair : Pair<APR, List<RuleConstraint>> = Pair(apr, listOf())
            val nodeWithExp = nodeToSubstitute.withExpansionTemporary(apr, apr.makeChildren(), { newNode ->
                pair = Pair(apr,ag.constraints[apr.rule]?.generate(attrs = newNode.attributes(), random = this.random) ?: listOf())
            })
            pair
        }
        return agExpansionConstraints.map { entry ->
            val apr = entry.first
            val constraints = (entry.second + (additionalConstraints)).toSet()
            var canMakeProgramTrade : Pair<Boolean, List<List<RuleConstraint>>>
            if(constraints.isEmpty()) {
                // No constraints!
                canMakeProgramTrade = Pair(true, apr.noConstraints)
            }
            else {
                val fittingAttributes = constraints.map {
                    (it.makeSatisfyingAttribute(random))
                }.distinct() //Make a set of attributes that would satisfy these constraints.
                // TODO: Sometimes satisfying attributes overlap and have the same keys. The current approach is just to fail these cases,
                // and then hope we have enough retires to randomly generate a set of attributes with no conflicts. A better approach would be to
                // SAT and create a set of satisfying attributes in one try.
                if(!isValidAttributeList(fittingAttributes)) {
                    canMakeProgramTrade = apr.cantMakeProgramReturn
                }
                else {
                    canMakeProgramTrade = apr.canMakeProgramWithAttributes(NodeAttributes.fromList(fittingAttributes))
                }
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
    fun expandNode(node: GenericGrammarNode, additionalConstraints : List<RuleConstraint> = listOf(), generationConfig : GenerationConfig = defaultGenerationConfig, depth : Int = 0, scope : CoroutineScope = GlobalScope) : Boolean {
        // First, create the program by expanding the start symbol.
        if(depth > generationConfig.maxProgramDepth) {
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
        var tryCount = 0
        while ((tryCount < generationConfig.numRandomTries || generationConfig.numRandomTries == -1)) {
            tryCount += 1
            val substitutedConstraints = this.getConstraintSubstitutions(node, expansions, additionalConstraints)
//            if(substitutedConstraints.size == 0) {
//                //Not possible to expand this.
//                return false;
//            }
//            val ruleDistribution = generationConfig.ruleWeights.filter {
//                it in substitutedConstraints.keys
//            }
//            val weighedOrderedPicks = ruleDistribution.sampledList(random)
            val weighedOrderedPicks = substitutedConstraints.keys.shuffled(random)
            // For each rule + constraints, see if we can expand every node there.
            for(expansion in weighedOrderedPicks) {
                val allNewConstraints = substitutedConstraints[expansion]!!
                var expansionIsGood = true
                //Make a set of unexpanded/terminal children
                val newChildren : List<GenericGrammarNode> = expansion.makeChildren()
                //Now, just expand the children trees.
                node.withExpansionTemporary(expansion, newChildren, {
                    if(!scope.isActive) {
                        throw TimeoutException()
                    }
                    for(i in it.rhs.indices){
                        val child = it.rhs[i]
                        //Expand each unexpanded child with all it's new constraints.
                        if(child.isUnexpanded()) {
                            val canExpand = expandNode(child, allNewConstraints[i], generationConfig, depth + 1, scope)
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
                    for(cons in additionalConstraints) {
                        check(cons.satisfies(node.attributes())) {
                            "Node $node doesn't satisfy needed constraint:\n$cons"
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    fun generate(rootConstraints: List<RuleConstraint> = listOf(), config: GenerationConfig = defaultGenerationConfig): RootGrammarNode = runBlocking {
        var program = RootGrammarNode(UnexpandedAPR(ag.start))
        var success: Boolean
        try {
            withTimeout(timeMillis = timeoutMs) {
                success = expandNode(program, rootConstraints, config, scope = this)
            }
        } catch (ex: TimeoutException) {
            // We ran out of time.
            if(returnPartialOnTimeout) {
                return@runBlocking program
            }
            else {
                success = false
            }
        }
        if(!success) {
            program = RootGrammarNode(UnexpandedAPR(ag.start))
        }
        program
    }
}