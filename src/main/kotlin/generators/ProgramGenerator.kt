package generators

import grammar.RootGrammarNode
import grammar.*
import grammar.constraints.RuleConstraint
import grammars.common.UnexpandedAPR
import java.util.*
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.*
class ProgramGenerator(val ag: AttributeGrammar, val numRandomTries : Int = 3, val timeoutMs : Long = 10000L) {


    /**
     * Given a list of APRs and a list of attributes we want to make,
     * return a map from APRs in that list to new constraints we could substitute them out for.
     */
    fun getConstraintSubstitutions(expansions: List<AttributedProductionRule>, fittingAttributes: List<NodeAttribute>): Map<AttributedProductionRule, List<RuleConstraint>> {
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
        if(!node.isUnexpanded()){
            return true // All done!
        }
        val expansions = ag.getPossibleExpansions(lhsSymbol)
        var foundSatisfying = false
        var tryCount = 0
        while (!foundSatisfying && (tryCount < numRandomTries || numRandomTries == -1)) {
            tryCount += 1
            val fittingAttributes = additionalConstraints.map { //Make a set of attributes that would satisfy these constraints.
                (it.makeSatisfyingAttribute())
            }

            val substitutedConstraints = this.getConstraintSubstitutions(expansions, fittingAttributes)
            // For each rule + constraints, see if we can expand every node there.
            substitutedConstraints.forEach { ruleEntry ->
                val attributes = node.attributes()
                val expansion = ruleEntry.key
                val ruleConstraints = ag.constraints[expansion]?.generate(attributes) ?: listOf()
                val allNewConstraints = ruleEntry.value + ruleConstraints // All the new constraints our expansion would need.
                val allFittingAttributes = allNewConstraints.map { //Make a set of attributes that would satisfy these constraints.
                    (it.makeSatisfyingAttribute())
                }
                
                var newChildren : List<GenericGrammarNode>
                var expansionIsGood = true
                if(allNewConstraints.isEmpty()){
                    //We have no constraints. Generate anything we want.
                    newChildren = expansion.makeChildren()
                }
                else {
                    // For each symbol in our grammar, find one that gives us a satisfying subprogram.
                    var satisfyingSubprogram : RootGrammarNode? = null
                    for(symbol in ag.symbols) {
                        satisfyingSubprogram = RootGrammarNode(UnexpandedAPR(symbol))
                        val canExpand = expandNode(satisfyingSubprogram, allNewConstraints) //Make a satisfying subprogram for the make...() call.
                        if(canExpand) {
                            break
                        }
                    }
                    // TODO: Handle rules that create/require more than 1 attribute.
                    newChildren = expansion.makeChildrenForAttribute(allFittingAttributes[0], nodeThatFits = satisfyingSubprogram)
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
                    node.withChildren(newChildren)
                    node.productionRule = expansion
                    return true
                }
            }
        }
        return false //
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