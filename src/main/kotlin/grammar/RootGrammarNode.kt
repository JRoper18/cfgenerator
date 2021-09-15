package grammar

class RootGrammarNode(productionRule: AttributedProductionRule) : GenericGrammarNode(productionRule){
    override fun inheritedAttributes(): Set<NodeAttribute> {
        return setOf()
    }
}
