package grammar
class GrammarNode(productionRule: AttributedProductionRule,
                  var parent: GrammarNode,
                  var idx: Int = 0) : GenericGrammarNode(productionRule){

    override val attributes: Set<NodeAttribute> by lazy {
        synthesizedAttributes.union(inheritedAttributes)
    }

    override val synthesizedAttributes: Set<NodeAttribute> by lazy {
        productionRule.makeSynthesizedAttributes(inheritedAttributes, rhs.map {
            it.attributes
        })
    }

    override val inheritedAttributes: Set<NodeAttribute> by lazy {
        val siblingAttrs = parent.rhs.map { grammarNode: GrammarNode ->
            grammarNode.synthesizedAttributes
        }
        productionRule.makeInheritedAttributes(idx, parent.attributes, siblingAttrs)
    }

}