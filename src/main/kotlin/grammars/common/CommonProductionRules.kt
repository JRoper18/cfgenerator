package grammars.common

import grammar.*
import grammar.constraints.IntBoundRuleConstraint


fun TerminalProductionRule(lhs: Symbol) : ProductionRule{
    return ProductionRule(lhs, listOf(StringSymbol("")))
}
fun ListProductionRule(listName: NtSym, unitName: Symbol, separator: String = ""): ProductionRule{
    if(separator.isEmpty()){
        return ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName))
    }
    return ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName))
}
class InitIntProductionRule(rule: ProductionRule, val initialKey : String, val initialVal : Int) : AttributedProductionRule(rule) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val ret = NodeAttributes()
        ret.setAttribute(initialKey, initialVal)
        return ret
    }
}
class InitStringProductionRule(rule: ProductionRule, val initialKey : String, val initialVal : String) : AttributedProductionRule(rule) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val ret = NodeAttributes()
        ret.setAttribute(initialKey, initialVal)
        return ret
    }
}
class BoundedListProductionRule(
    listName: NtSym,
    unitName: Symbol,
    separator: String = "",
    val minimumSize: Int = -1,
    val maximumSize: Int = -1) : AttributedProductionRule(
    ListProductionRule(listName, unitName, separator)) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val listAttrs = childAttributes[0]
        val sizeKey = "length"
        val ret = listAttrs.copy()
        ret.setAttribute(sizeKey, listAttrs.getIntAttribute(sizeKey)!! + 1)
        return ret
    }
}
class BoundedListBeginProductionRule(listName: NtSym,
                                unitName: NtSym,
                                separator: String = "",
                                val minimumSize : Int? = null,
                                val maximumSize : Int? = null) : AttributedProductionRule(
    ListProductionRule(listName, unitName, separator), listOf(IntBoundRuleConstraint("length", minimumSize, maximumSize))) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val listAttrs = childAttributes[0]
        val sizeKey = "length"
        val ret = listAttrs.copy()
        ret.setAttribute(sizeKey, listAttrs.getIntAttribute(sizeKey)!! + 1)
        return ret
    }
}
val LowercaseASCIISymbol = StringsetSymbol("qwertyuiopasdfghjklzxcvbnm".split(""), "lowercaseASCII")
fun StringSetProductionRules(symbol: StringsetSymbol) : List<AttributedProductionRule> {
    return symbol.stringset.map {
        APR(PR(symbol, listOf(StringSymbol(it))))
    }
}
fun LowercaseASCIIProductionRule(symbolName: String) : ProductionRule {
    return ProductionRule(NtSym(symbolName), listOf(LowercaseASCIISymbol))
}
