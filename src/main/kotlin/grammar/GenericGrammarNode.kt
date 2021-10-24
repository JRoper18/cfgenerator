package grammar

import grammars.common.TerminalAPR
import grammars.common.UnexpandedAPR


sealed class GenericGrammarNode(
    productionRule: AttributedProductionRule
){
    var productionRule : AttributedProductionRule = productionRule
    set(value) {
        clearAttributeCache()
        field = value
    }

    private var cachedSynthAttrs : NodeAttributes? = null
    private var cachedInheritedAttrs : NodeAttributes? = null
    private var cachedAttrs : NodeAttributes? = null
    protected fun clearAttributeCache() {
        cachedSynthAttrs = null
        cachedAttrs = null
        cachedInheritedAttrs = null
    }
    private fun isSynthesizedCached() : Boolean {
        return cachedSynthAttrs != null
    }
    private fun isAttributesCached() : Boolean {
        return cachedAttrs != null
    }
    var rhs: List<GrammarNode> = listOf()
        private set // People shouldn't set this field because they may forget to set parent pointers.
    fun lhsSymbol() : Symbol {
        return productionRule.rule.lhs
    }
    fun attributes(): NodeAttributes {
        if(isAttributesCached()) {
            return cachedAttrs!!
        }
        val ret = synthesizedAttributes().union(inheritedAttributes())
        cachedAttrs = ret
        return ret
    }
    fun synthesizedAttributes(): NodeAttributes {
        if(isSynthesizedCached()) {
            return cachedSynthAttrs!!
        }
        if(this.isUnexpanded()) {
            return NodeAttributes()
        }
        val ret = productionRule.makeSynthesizedAttributes(rhs.map {
            it.attributes()
        })
        cachedSynthAttrs = ret
        return ret
    }
    abstract fun inheritedAttributes(): NodeAttributes
    abstract fun depth() : Int

    fun withParent(parent: GenericGrammarNode, index : Int) : GrammarNode {
        val node = GrammarNode(productionRule, parent, index)
        node.withChildren(this.rhs)
        this.cachedInheritedAttrs = null
        this.cachedAttrs = null
        return node
    }

    fun withChildren(children: List<GenericGrammarNode>): GenericGrammarNode {
        this.cachedSynthAttrs = null
        this.cachedAttrs = null
        this.rhs = children.mapIndexed { index, child ->
            child.withParent(this, index)
        }
        return this
    }

    fun withExpansionTemporary(rule: APR, children : List<GenericGrammarNode>, f : (GenericGrammarNode) -> Unit, keep : () -> Boolean = {
        false
    }) {
        val oldSynthCache = cachedSynthAttrs
        val oldInheritedCache = cachedInheritedAttrs
        val oldAttrCache = cachedAttrs
        this.clearAttributeCache()
        this.withChildren(children)
        this.productionRule = rule
        f(this)
        if(!keep()){
            this.productionRule = UnexpandedAPR(this.lhsSymbol())
            this.withChildren(listOf())
            cachedAttrs = oldAttrCache
            cachedInheritedAttrs = oldInheritedCache
            cachedSynthAttrs = oldSynthCache
        }
    }

    override fun toString(): String {
        return this.toString(printAttrs = true, printAPR = false, splitAttrs = false)
    }
    fun toString(printAttrs: Boolean = true, printAPR : Boolean = true, splitAttrs : Boolean = false): String {
        val buffer = StringBuilder(50)
        print(buffer, "", "", printAttrs, printAPR)
        return buffer.toString()
    }

    protected fun print(buffer: StringBuilder, prefix: String, childrenPrefix: String,
                        printAttrs : Boolean = true,
                        printAPR: Boolean = false,
                        splitAttrs : Boolean = false) {
        buffer.append(prefix)
        if(printAPR) {
            buffer.append(this.productionRule.toString())
        }
        else {
            buffer.append(productionRule.rule.toString())
        }
        if(printAttrs){
            buffer.append(' ')
            if(splitAttrs) {
                buffer.append(this.synthesizedAttributes())
                buffer.append(" I ${this.inheritedAttributes()}")
            }
            else {
                buffer.append(this.attributes())
            }
        }
        buffer.append('\n')
        val it = rhs.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (it.hasNext()) {
                next.print(buffer, "$childrenPrefix├── ", "$childrenPrefix│   ", printAttrs = printAttrs, printAPR = printAPR)
            } else {
                next.print(buffer, "$childrenPrefix└── ", "$childrenPrefix    ", printAttrs = printAttrs, printAPR = printAPR)
            }
        }
    }
    fun isUnexpanded() : Boolean {
        return ((this.rhs.size != this.productionRule.rule.rhs.size && !this.lhsSymbol().terminal) ||
                this.productionRule.rule == UnexpandedAPR(this.lhsSymbol()).rule)
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

        this.rhs.forEach {
            check(it.parent === this) {
                "Child pointers to parent must be the parent. \nExpected parent: \n${this}\nActual:\n${it.parent}it"
            }
        }

        // Repeat verification on the children.
        this.rhs.forEach {
            it.verify()
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
