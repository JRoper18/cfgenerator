package grammars.common

import grammar.*
import grammar.StringsetSymbol
import grammar.constraints.RuleConstraint

val TERMINAL = StringSymbol("")
fun TerminalProductionRule(lhs: Symbol) : ProductionRule{
    return ProductionRule(lhs, listOf(TERMINAL))
}
fun TerminalAPR(lsh: Symbol) : APR {
    return APR(TerminalProductionRule(lsh))
}
fun UnexpandedAPR(lsh: Symbol) : APR {
    return APR(TerminalProductionRule(lsh))
}
fun ListProductionRule(listName: NtSym, unitName: Symbol, separator: String = ""): ProductionRule{
    if(separator.isEmpty()){
        return ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName))
    }
    return ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName))
}
fun makeStringsetRules(symbol: StringsetSymbol) : List<AttributedProductionRule> {
    println()
    return symbol.stringset.map {
        StringsetSymbolRule(symbol, StringSymbol(it))
    }
}
class StringsetSymbolRule(val stringSetSymbol : StringsetSymbol, val stringSymbol : StringSymbol) : AttributedProductionRule(PR(stringSetSymbol, listOf(stringSymbol))) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        return NodeAttributes(mutableMapOf("chosenSymbol" to stringSymbol.name))
    }

    override fun canMakeProgramWithAttribute(attr: NodeAttribute): Pair<Boolean, List<RuleConstraint>> {
        return Pair(attr.first == "chosenSymbol" && attr.second == stringSymbol.name, listOf())
    }
}
val LowercaseASCIISymbol = StringsetSymbol("qwertyuiopasdfghjklzxcvbnm".split("").filter {
    it.isNotBlank()
}.toSet(), "lowercaseASCII")
fun LowercaseASCIIProductionRule(symbolName: String) : ProductionRule {
    return ProductionRule(NtSym(symbolName), listOf(LowercaseASCIISymbol))
}
