package grammar
class GrammarNode(
    productionRule: AttributedProductionRule,
    val parent: GenericGrammarNode,
    var idx: Int = 0) : GenericGrammarNode(productionRule){

    override fun inheritedAttributes(): Set<NodeAttribute> {
        val siblingAttrs = mutableListOf<Set<NodeAttribute>>()
        for(i in 0..idx-1){ // For every left-node:
            siblingAttrs.add(parent.rhs[i].synthesizedAttributes())
        }
        return productionRule.makeInheritedAttributes(idx, parent.inheritedAttributes(), siblingAttrs)
    }

}