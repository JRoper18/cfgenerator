package grammar

open class AttributedProductionRule(var rule: ProductionRule) {
    // Given a set of node attributes for each node on the RHS and my inherited attrs, return a set of new NodeAttributes.
    open fun makeSynthesizedAttributes(childAttributes: List<Set<NodeAttribute>>): Set<NodeAttribute> {
        return setOf()
    }

    // Given a set of node attributes for each sibling node less than us, the index of the RHS node, and the parent attributes, return a set of new NodeAttributes.
    // Again, all nodes to the LEFT of us aka less index than us in the sibling order.
    // That means we're going L grammars.
    open fun makeInheritedAttributes(myIdx: Int, parentAttributes: Set<NodeAttribute>, siblingAttributes: List<Set<NodeAttribute>>): Set<NodeAttribute> {
        return setOf()
    }

    open fun satisfiesConstraints(attributes: Set<NodeAttribute>) : Boolean {
        return true;
    }
}