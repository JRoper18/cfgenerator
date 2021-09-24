package grammar

import grammars.common.TerminalAPR

class GrammarNode(
    productionRule: AttributedProductionRule,
    val parent: GenericGrammarNode,
    var idx: Int = 0) : GenericGrammarNode(productionRule){

    constructor(lhs: Symbol, parent: GenericGrammarNode, idx: Int) : this(TerminalAPR(lhs), parent, idx)
    override fun inheritedAttributes(): NodeAttributes {
        val siblingAttrs = mutableListOf<NodeAttributes>()
        for(i in 0..idx-1){ // For every left-node:
            siblingAttrs.add(parent.rhs[i].synthesizedAttributes())
        }
        return productionRule.makeInheritedAttributes(idx, parent.inheritedAttributes(), siblingAttrs)
    }

    override fun depth(): Int {
        return parent.depth() + 1
    }


}