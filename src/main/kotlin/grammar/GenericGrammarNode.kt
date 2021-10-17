package grammar

import grammars.common.TerminalAPR


sealed class GenericGrammarNode(var productionRule: AttributedProductionRule){
    var rhs: List<GrammarNode> = listOf()
    fun lhsSymbol() : Symbol {
        return productionRule.rule.lhs
    }
    fun attributes(): NodeAttributes {
        return synthesizedAttributes().union(inheritedAttributes())
    }
    fun synthesizedAttributes(): NodeAttributes {
        if(this.isUnexpanded()) {
            return NodeAttributes()
        }
        return productionRule.makeSynthesizedAttributes(rhs.map {
            it.attributes()
        })
    }
    abstract fun inheritedAttributes(): NodeAttributes
    abstract fun depth() : Int

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
    fun withChildren(children: List<GenericGrammarNode>): GenericGrammarNode {
        this.rhs = children.mapIndexed { index, child ->
            child.withParent(this, index)
        }
        return this
    }


    override fun toString(): String {
        return this.toString(printAttrs = true)
    }
    fun toString(printAttrs: Boolean = true): String {
        val buffer = StringBuilder(50)
        print(buffer, "", "", printAttrs)
        return buffer.toString()
    }

    protected fun print(buffer: StringBuilder, prefix: String, childrenPrefix: String, printAttrs : Boolean = true) {
        buffer.append(prefix)
        buffer.append(productionRule.rule.toString())
        if(printAttrs){
            buffer.append(' ')
            buffer.append(this.attributes())
        }
        buffer.append('\n')
        val it = rhs.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (it.hasNext()) {
                next.print(buffer, "$childrenPrefix├── ", "$childrenPrefix│   ", printAttrs = printAttrs)
            } else {
                next.print(buffer, "$childrenPrefix└── ", "$childrenPrefix    ", printAttrs = printAttrs)
            }
        }
    }
    fun isUnexpanded() : Boolean {
        return (this.rhs.size != this.productionRule.rule.rhs.size && !this.lhsSymbol().terminal)
    }
    fun getUnexpandedNodes() : List<GenericGrammarNode> {
        val thisUnexpanded = if(isUnexpanded()) listOf(this) else listOf()
        return this.rhs.flatMap {
            it.getUnexpandedNodes()
        } + thisUnexpanded
    }

    open fun verify() {
        check(lhsSymbol().equals(this.productionRule.rule.lhs)) {
            "Our left hand symbol needs to match the symbol in our production rule. "
        }
        check(productionRule.rule.rhs.size == this.rhs.size) {
            "Our rule says our RHS has ${productionRule.rule.rhs.size} elements but we have ${this.rhs.size}"
        }
        val unexpanded = getUnexpandedNodes()
        check(unexpanded.isEmpty()) {
            var str = "There are ${unexpanded.size} unexpanded nodes present:"
            unexpanded.forEach {
                str += "$it\n"
            }
            str
        }
        productionRule.rule.rhs.forEachIndexed { index, symbol ->
            val actualSym = this.rhs[index].lhsSymbol()
            check(actualSym.equals(symbol)) {
                "Our RHS symbol at index ${index} should be $symbol but is $actualSym"
            }
        }
    }

    fun symbolCount(symbol : Symbol) : Int {
        var count = 0;
        if(lhsSymbol().equals(symbol)){
            count += 1;
        }
        for(child in this.rhs) {
            count += (child.symbolCount(symbol))
        }
        return count;
    }

    fun forEachInTree(f: (node : GenericGrammarNode) -> Unit){
        f(this)
        for(child in this.rhs) {
            f(child)
            child.forEachInTree(f)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericGrammarNode) return false

        if (productionRule != other.productionRule) return false
        if (rhs != other.rhs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productionRule.hashCode()
        result = 31 * result + rhs.hashCode()
        return result
    }

}
