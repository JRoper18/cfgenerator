package grammars.common.rules

import grammar.*
import grammar.StringsetSymbol

val TERMINAL = StringSymbol("")
val UNEXPANDED = StringSymbol("UNEXPANDED")
val SPACE = StringSymbol(" ")
val LP = StringSymbol("(")
val RP = StringSymbol(")")
val LSB = StringSymbol("[")
val RSB = StringSymbol("]")
val COLON = StringSymbol(":")
val COMMA = StringSymbol(",")
fun TerminalProductionRule(lhs: Symbol) : ProductionRule{
    return ProductionRule(lhs, listOf())
}
fun TerminalAPR(lsh: Symbol) : APR {
    return APR(TerminalProductionRule(lsh))
}
// Used during the generation process. 
// If you're seeing UNEXPANDED symbols in a generated program, there's something wrong with the grammar or the generator. 
fun UnexpandedAPR(lsh: Symbol) : APR {
    return APR(PR(lsh, listOf(UNEXPANDED)))
}
fun makeStringsetRules(symbol: StringsetSymbol) : List<Pair<String, AttributedProductionRule>> {
    return symbol.stringset.map {
        Pair(it, StringsetSymbolRule(symbol, StringSymbol(it)))
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

