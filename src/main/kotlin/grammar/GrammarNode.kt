package grammar
class GrammarNode(
    productionRule: AttributedProductionRule,
    val parent: GenericGrammarNode,
    var idx: Int = 0) : GenericGrammarNode(productionRule){

    override fun inheritedAttributes(): NodeAttributes {
        val siblingAttrs = mutableListOf<NodeAttributes>()
        for(i in 0..idx-1){ // For every left-node:
            siblingAttrs.add(parent.rhs[i].synthesizedAttributes())
        }
        return productionRule.makeInheritedAttributes(idx, parent.inheritedAttributes(), siblingAttrs)
    }

}