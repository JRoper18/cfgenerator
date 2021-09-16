package grammar

import javax.swing.tree.TreeNode




sealed class GenericGrammarNode(var productionRule: AttributedProductionRule){
    var rhs: List<GrammarNode> = listOf()
    fun lhsSymbol() : Symbol {
        return productionRule.rule.lsh
    }
    fun attributes(): Set<NodeAttribute> {
        return synthesizedAttributes().union(inheritedAttributes())
    }
    fun synthesizedAttributes(): Set<NodeAttribute> {
        return productionRule.makeSynthesizedAttributes(rhs.map {
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

    override fun toString(): String {
        val buffer = StringBuilder(50)
        print(buffer, "", "")
        return buffer.toString()
    }

    protected fun print(buffer: StringBuilder, prefix: String, childrenPrefix: String) {
        buffer.append(prefix)
        buffer.append(productionRule.rule.toString())
        buffer.append('\n')
        val it = rhs.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (it.hasNext()) {
                next.print(buffer, "$childrenPrefix├── ", "$childrenPrefix│   ")
            } else {
                next.print(buffer, "$childrenPrefix└── ", "$childrenPrefix    ")
            }
        }
    }

}
