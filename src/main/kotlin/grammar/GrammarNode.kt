package grammar

import grammars.common.TerminalAPR

class GrammarNode(
    productionRule: AttributedProductionRule,
    val parent: GenericGrammarNode,
    var idx: Int = 0) : GenericGrammarNode(productionRule){

    constructor(lhs: Symbol, parent: GenericGrammarNode, idx: Int) : this(TerminalAPR(lhs), parent, idx)
    override fun inheritedAttributes(): NodeAttributes {
        val siblingAttrs = mutableListOf<NodeAttributes>()
        for(i in 0 until idx){ // For every left-node:
            siblingAttrs.add(parent.rhs[i].synthesizedAttributes())
        }
        val parentAttrs = parent.inheritedAttributes()
        if(this.productionRule.rule.lhs.name == "VarName") {
//            println("Node")
//            println(parentAttrs)
//            println("Sibling")
//            println(siblingAttrs)
//            println("synth")
//            println(this.synthesizedAttributes())
        }
        return productionRule.makeInheritedAttributes(idx, parentAttrs, siblingAttrs)
    }

    override fun depth(): Int {
        return parent.depth() + 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GrammarNode) return false
        if (!super.equals(other)) return false

        if (parent != other.parent) return false
        if (idx != other.idx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + parent.hashCode()
        result = 31 * result + idx
        return result
    }


}