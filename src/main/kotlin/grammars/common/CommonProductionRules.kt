package grammars.common

import StringsetSymbolRule
import grammar.*
import grammar.StringsetSymbol
import grammar.constraints.RuleConstraint

val TERMINAL = StringSymbol("")
val UNEXPANDED = StringSymbol("UNEXPANDED")
val SPACE = StringSymbol(" ")
val LP = StringSymbol("(")
val RP = StringSymbol(")")
fun TerminalProductionRule(lhs: Symbol) : ProductionRule{
    return ProductionRule(lhs, listOf())
}
fun TerminalAPR(lsh: Symbol) : APR {
    return APR(TerminalProductionRule(lsh))
}
fun UnexpandedAPR(lsh: Symbol) : APR {
    return APR(PR(lsh, listOf(UNEXPANDED)))
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
fun intSymbols(from : Int, to : Int) : Set<String> {
    return (from until to).map {
        it.toString()
    }.filter {
        it.isNotBlank()
    }.toSet()
}