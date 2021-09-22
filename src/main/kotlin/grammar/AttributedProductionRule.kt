package grammar

import grammar.constraints.RuleConstraint

open class AttributedProductionRule(val rule: ProductionRule) {
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
     * and if needed, a list of additional constraints we'll need to fill in the true case.
     * This list is empty if we don't need to fill any more constraints.
     * MUST be deterministic, because the
     */
    open fun canMakeProgramWithAttribute(attr: NodeAttribute) : Pair<Boolean, List<RuleConstraint>> {
        return Pair(false, listOf())
    }

    /**
     * Assume someone's program has the rules needed. Here, we assemble a program with the given nodes.
     * If we return a list of constraints, the input is a list of programs that satisfy the constrains in the order we return them.
     */
    open fun makeProgramWithAttribute(attr: NodeAttribute, node : GenericGrammarNode? = null) : GenericGrammarNode? {
        return null
    }
}