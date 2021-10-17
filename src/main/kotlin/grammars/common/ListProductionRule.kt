package grammars.common

import grammar.*

data class ListProductionRule(val listName: NtSym, val unitName: Symbol, val separator: String = "") :
    ProductionRule(listName, listOf(listName, StringSymbol(separator), unitName)) {
    fun unroll(listNode: GenericGrammarNode) : List<GenericGrammarNode> {
        require(listNode.productionRule.rule == this)
        val list = listNode.rhs[0]
        val unit = listNode.rhs[2]
        if(list.productionRule.rule is ListProductionRule) {
            return (list.productionRule.rule as ListProductionRule).unroll(list) + listOf(unit)
        }
        else {
            // Our sublist is a terminator/other version of a list.
            return listOf(unit)
        }
    }
}
