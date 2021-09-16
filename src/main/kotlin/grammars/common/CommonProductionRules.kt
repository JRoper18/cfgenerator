package grammars.common

import grammar.*


fun TerminalProductionRule(lhs: Symbol) : AttributedProductionRule{
    return AttributedProductionRule(ProductionRule(lhs, listOf(StringSymbol(""))))
}
fun ListProductionRule(listName: String, unitName: String, separator: String = ""): ProductionRule{
    return ListProductionRule(NtSym(listName), NtSym(unitName), separator)
}
fun ListProductionRule(listName: NtSym, unitName: NtSym, separator: String = ""): ProductionRule{
    if(separator.isEmpty()){
        return ProductionRule(listName, listOf(listName, unitName))
    }
    return ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName))
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
