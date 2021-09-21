package grammar

import grammar.constraints.RuleConstraint

open class AttributedProductionRule(var rule: ProductionRule, val constraints: List<RuleConstraint> = listOf()) {
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

    open val modified

    /**
     * Given a constraint, we can generate a program that fits that constraint.
     */


    fun failingConstraints(attrs: NodeAttributes) : List<RuleConstraint> {
        return constraints.filter {
            !it.satisfies(attrs)
        }
    }

    fun satisfiesConstraints(attrs: NodeAttributes) : Boolean {
        return failingConstraints(attrs).isEmpty()
    }
}