package grammar

class RootGrammarNode(productionRule: AttributedProductionRule) : GenericGrammarNode(productionRule){
    override fun inheritedAttributes(): NodeAttributes {
        return NodeAttributes()
    }

    override fun depth(): Int {
        return 0
    }
}
