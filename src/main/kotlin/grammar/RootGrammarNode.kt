package grammar

class RootGrammarNode(productionRule: AttributedProductionRule) : GenericGrammarNode(productionRule){
    override fun inheritedAttributes(): NodeAttributes {
        return NodeAttributes()
    }
}
