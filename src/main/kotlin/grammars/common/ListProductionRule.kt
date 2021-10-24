package grammars.common

import grammar.*

class ListProductionRule(val listName: NtSym, val unitName: Symbol, val separator: String = "") :
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
            // TODO: What if the LHS is a list but like of size 1 or they custom make a rule for that?
            // Solution: Fuck them. And by them I mean me. I make a list rule with 1 unit as the RHS. Too hard to find solution for all cases here.
            return listOf(unit)
        }
    }
}
