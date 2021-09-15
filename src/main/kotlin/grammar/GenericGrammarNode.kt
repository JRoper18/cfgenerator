package grammar

sealed class GenericGrammarNode(val productionRule: AttributedProductionRule){
    var rhs: List<GrammarNode> = listOf()
    val lhsSymbol : Symbol by lazy {
        productionRule.rule.lsh
    }
    abstract val attributes: Set<NodeAttribute>
    abstract val synthesizedAttributes: Set<NodeAttribute>
    abstract val inheritedAttributes: Set<NodeAttribute>

    fun withChildren(makeChildren: (parent: GenericGrammarNode) -> List<GrammarNode>): GenericGrammarNode {
        this.rhs = makeChildren(this)
        this.rhs.forEachIndexed { index, grammarNode ->
            grammarNode.idx = index
        }
        return this
    }

}
