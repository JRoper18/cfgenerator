package grammars.common.rules

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
            // Our sublist is a single init version of a list.
            return listOf(list.rhs[0], unit)
        }
    }
    fun roll(units : List<GenericGrammarNode>, listInitRule : APR, parentAPR : APR) : GenericGrammarNode {
        require(parentAPR.rule == this) {
            "Parent APR must be a list production rule!"
        }
        if(units.size == 1) {
            return RootGrammarNode(listInitRule).withChildren(units)
        }
        val unitNode = units.last()
        val listNode = roll(units.subList(0, units.size - 1), listInitRule, parentAPR)
        return RootGrammarNode(parentAPR).withChildren(listOf(
            listNode,
            RootGrammarNode(TerminalAPR(this.rhs[1])),
            unitNode
        ))
    }
}
