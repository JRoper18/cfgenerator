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
fun makeStringsetRules(symbol: StringsetSymbol) : List<AttributedProductionRule> {
    return symbol.stringset.map {
        StringsetSymbolRule(symbol, StringSymbol(it))
    }
}
val lowercaseASCII = "qwertyuiopasdfghjklzxcvbnm".split("").filter {
    it.isNotBlank()
}.toSet()
val LowercaseASCIISymbol = StringsetSymbol(lowercaseASCII, displayName = "lowercaseASCII")
