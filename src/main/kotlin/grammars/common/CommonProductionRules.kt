package grammars.common

import StringsetSymbolRule
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
    return symbol.stringset.map {
        StringsetSymbolRule(symbol, StringSymbol(it))
    }
}
val LowercaseASCIISymbol = StringsetSymbol("qwertyuiopasdfghjklzxcvbnm".split("").filter {
    it.isNotBlank()
}.toSet(), displayName = "lowercaseASCII")
fun LowercaseASCIIProductionRule(symbolName: String) : ProductionRule {
    return ProductionRule(NtSym(symbolName), listOf(LowercaseASCIISymbol))
}
