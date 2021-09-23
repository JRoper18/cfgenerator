package grammar

import grammars.common.TerminalAPR


sealed class GenericGrammarNode(var productionRule: AttributedProductionRule){
    var rhs: List<GrammarNode> = listOf()
    fun lhsSymbol() : Symbol {
        return productionRule.rule.lsh
    }
    fun attributes(): NodeAttributes {
        return synthesizedAttributes().union(inheritedAttributes())
    }
    fun synthesizedAttributes(): NodeAttributes {
        return productionRule.makeSynthesizedAttributes(rhs.map {
            it.attributes()
        })
    }
    abstract fun inheritedAttributes(): NodeAttributes

    fun withParent(parent: GenericGrammarNode, index : Int) : GrammarNode {
        val node = GrammarNode(productionRule, parent, index)
        node.rhs = this.rhs
        return node
    }

    fun withChildSymbols(unexpandedData : List<Symbol>) : GenericGrammarNode {
        this.rhs = unexpandedData.mapIndexed { index, symbol ->
            GrammarNode(TerminalAPR(symbol), this, index)
        }
        return this
    }
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

    fun getUnexpandedNodes() : List<GenericGrammarNode> {
        val thisUnexpanded = if(this.rhs.size == this.productionRule.rule.rhs.size || this.lhsSymbol().terminal) listOf() else listOf(this)
        return this.rhs.flatMap {
            it.getUnexpandedNodes()
        } + thisUnexpanded
    }

    open fun verify() {
        check(lhsSymbol().equals(this.productionRule.rule.lsh)) {
            "Our left hand symbol needs to match the symbol in our production rule. "
        }
        check(productionRule.rule.rhs.size == this.rhs.size) {
            "Our rule says our RHS has ${productionRule.rule.rhs.size} elements but we have ${this.rhs.size}"
        }
        val unexpanded = getUnexpandedNodes()
        check(unexpanded.isEmpty()) {
            "There are unexpanded nodes present: $unexpanded"
        }
        productionRule.rule.rhs.forEachIndexed { index, symbol ->
            val actualSym = this.rhs[index].lhsSymbol()
            check(actualSym.equals(symbol)) {
                "Our RHS symbol at index ${index} should be $symbol but is $actualSym"
            }
        }
    }

}
