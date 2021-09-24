package generators

import grammar.RootGrammarNode
import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.UnexpandedAPR
import java.util.*
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.*
class ProgramGenerator(val ag: AttributeGrammar,
                       val numRandomTries : Int = 3,
                       val timeoutMs : Long = 10000L,
                       val maxProgramDepth : Int = 10) {


    /**
     * Given a list of APRs and a list of attributes we want to make,
     * return a map from APRs in that list to new constraints we could substitute them out for.
     */
    fun getConstraintSubstitutions(nodeAttributes: NodeAttributes, expansions: List<AttributedProductionRule>, fittingAttributes: List<NodeAttribute>): Map<AttributedProductionRule, List<RuleConstraint>> {
        return expansions.map { apr ->
            val satisfiesEachConstraint = fittingAttributes.map{ attr ->
                val canMakeData = apr.canMakeProgramWithAttribute(attr)
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
            val noExtra = value.value.flatMap {
                it.second
            }
            // And add the additional constraints from our grammar.
            val agConstraints = ag.constraints[value.key]?.generate(attrs = nodeAttributes) ?: listOf()
            noExtra + agConstraints
        } // And now we have a map from attributes to ALL their required constraints, only if they can actually be made (according to their functions).
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
        if(!node.isUnexpanded()){
            // If the node's unexpanded (ie terminal) then just return if it satisfies the constraints.
            return additionalConstraints.find {
                !it.satisfies(attributes)
            } == null // Return the first constraint that doesn't satisfy, null if they all satisfy.
        }
        val expansions = ag.getPossibleExpansions(lhsSymbol)
        var foundSatisfying = false
        var tryCount = 0
        while (!foundSatisfying && (tryCount < numRandomTries || numRandomTries == -1)) {
            tryCount += 1
            val fittingAttributes = additionalConstraints.map { //Make a set of attributes that would satisfy these constraints.
                (it.makeSatisfyingAttribute())
            }

            val substitutedConstraints = this.getConstraintSubstitutions(attributes, expansions, fittingAttributes)
            // For each rule + constraints, see if we can expand every node there.
            for(ruleEntry in substitutedConstraints) {
                val expansion = ruleEntry.key
                val allNewConstraints = ruleEntry.value
                //For our new set of constraints, generate a new set of satisfying attributes.
                val allNewFittingAttributes = allNewConstraints.map {
                    it.makeSatisfyingAttribute()
                }
                var newChildren : List<GenericGrammarNode>
                var expansionIsGood = true
                if(allNewConstraints.isEmpty()){
                    //We have no constraints. Generate anything we want.
                    newChildren = expansion.makeChildren()
                }
                else {
                    val satisfyingSubprograms = mutableListOf<GenericGrammarNode>()
                    for(fittingAttribute in allNewFittingAttributes) {
                        var satisfyingSubprogram : RootGrammarNode? = null
                        var foundSatisfyingSubprogram = false
                        // For each symbol in our grammar, find one that gives us a satisfying subprogram.
                        for(symbol in expansion.rule.rhs) {
                            satisfyingSubprogram = RootGrammarNode(UnexpandedAPR(symbol))
                            foundSatisfyingSubprogram = expandNode(satisfyingSubprogram, allNewConstraints, depth + 1) //Make a satisfying subprogram for the make...() call.
                            if(foundSatisfyingSubprogram) {
                                break
                            }
                        }
                        if(foundSatisfyingSubprogram) {
                            satisfyingSubprograms.add(satisfyingSubprogram!!)
                        }
                        else {
                            break;
                        }
                    }
                    if(fittingAttributes.isEmpty()) {
                        println(node)
                        println(allNewConstraints)
                        print(allNewFittingAttributes)
                        println("EMPTY FITTING")
                    }
                    // TODO: Handle rules that create/require more than 1 attribute.
                    newChildren = expansion.makeChildrenForAttribute(allNewFittingAttributes[0], nodesThatFit = satisfyingSubprograms)
                }
                //Now, just expand the children trees.
                for(child in newChildren){
                    for(unexpanded in child.getUnexpandedNodes()) {
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
                if(expansionIsGood) {
                    println(node)
                    node.withChildren(newChildren)
                    node.productionRule = expansion
                    println("AFTER")
                    println(node)
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