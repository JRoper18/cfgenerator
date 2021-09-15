package grammar

class RootGrammarNode(productionRule: AttributedProductionRule) : GenericGrammarNode(productionRule){
    override val attributes: Set<NodeAttribute> by lazy {
        synthesizedAttributes
    }

    override val synthesizedAttributes: Set<NodeAttribute> by lazy {
        productionRule.makeSynthesizedAttributes(inheritedAttributes, rhs.map {
            it.attributes
        })
    }

    override val inheritedAttributes: Set<NodeAttribute> = setOf()
}
