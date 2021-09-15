package grammar

sealed class GenericGrammarNode(var productionRule: AttributedProductionRule){
    var rhs: List<GrammarNode> = listOf()
    fun lhsSymbol() : Symbol {
        return productionRule.rule.lsh
    }
    fun attributes(): Set<NodeAttribute> {
        return synthesizedAttributes().union(inheritedAttributes())
    }
    fun synthesizedAttributes(): Set<NodeAttribute> {
        return productionRule.makeSynthesizedAttributes(inheritedAttributes(), rhs.map {
            it.attributes()
        })
    }
    abstract fun inheritedAttributes(): Set<NodeAttribute>

    fun withChildren(makeChildren: (parent: GenericGrammarNode) -> List<GrammarNode>): GenericGrammarNode {
        this.rhs = makeChildren(this)
        this.rhs.forEachIndexed { index, grammarNode ->
            grammarNode.idx = index
        }
        return this
    }

}
