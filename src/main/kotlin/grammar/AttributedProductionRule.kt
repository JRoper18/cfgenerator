package grammar

import grammar.constraints.RuleConstraint
import grammars.common.rules.TerminalAPR
import grammars.common.rules.UnexpandedAPR

open class AttributedProductionRule(val rule: ProductionRule) {

    val noConstraints by lazy {
        this.rule.rhs.map {
            listOf<RuleConstraint>()
        }
    }
    val cantMakeProgramReturn by lazy {
        Pair(false, noConstraints)
    }
    // Given a set of node attributes for each node on the RHS and my inherited attrs, return a set of new NodeAttributes.
    open fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        return NodeAttributes()
    }

    // Given a set of node attributes for each sibling node less than us, the index of the RHS node, and the parent attributes, return a set of new NodeAttributes.
    // Again, all nodes to the LEFT of us aka less index than us in the sibling order.
    // That means we're going L grammars.
    open fun makeInheritedAttributes(myIdx: Int, parentAttributes: NodeAttributes, siblingAttributes: List<NodeAttributes>): NodeAttributes {
        return NodeAttributes()
    }

    /**
     * Given a constraint, can we generate a program with that attribute?
     * Returns a boolean saying if we can do it or not,
     * and if needed, a list of additional constraints we'll need to fill in the true case, where the ith list corresponds to constraints
     *  for the ith child that are needed.
     * This list is empty if we don't need to fill any more constraints.
     * MUST be deterministic
     */
    open fun canMakeProgramWithAttributes(attrs: NodeAttributes) : Pair<Boolean, List<List<RuleConstraint>>> {
        return cantMakeProgramReturn
    }

    fun makeChildren() : List<GenericGrammarNode> {
        return this.rule.rhs.map {
            if(it.terminal) {
                RootGrammarNode(TerminalAPR(it))
            }
            else {
                RootGrammarNode(UnexpandedAPR(it))
            }
        }
    }

    fun makeRootProgramWithAttributes(attrs: NodeAttributes, nodesThatFit: List<GenericGrammarNode> = listOf()) : RootGrammarNode {
        val node = RootGrammarNode(this)
        node.withChildren(nodesThatFit)
        return node
    }

    override fun toString(): String {
        return "${this.javaClass.name}:(rule=$rule)"
    }

}