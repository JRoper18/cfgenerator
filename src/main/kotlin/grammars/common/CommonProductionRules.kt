package grammars.common

import grammar.*

fun ListProductionRule(listName: String, unitName: String, separator: String = ""): ProductionRule{
    if(separator.length == 0){
        return ProductionRule(NtSym(listName), listOf(NtSym(listName), NtSym(unitName)))
    }
    return ProductionRule(NtSym(listName), listOf(NtSym(listName), StringSymbol(separator), NtSym(unitName)))

}
fun ListProductionRule(listName: NtSym, unitName: NtSym, separator: String = ""): ProductionRule{
    if(separator.length == 0){
        return ProductionRule(listName, listOf(listName, unitName))
    }
    return ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName))

}
val LowercaseASCIISymbol = StringsetSymbol("qwertyuiopasdfghjklzxcvbnm".split(""))
fun LowercaseASCIIProductionRule(symbolName: String) : ProductionRule {
    return ProductionRule(NtSym(symbolName), listOf(LowercaseASCIISymbol))
}