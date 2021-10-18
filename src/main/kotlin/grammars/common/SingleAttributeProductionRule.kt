package grammars.common

import grammar.*
import grammar.constraints.RuleConstraint

abstract class SingleAttributeProductionRule(rule : ProductionRule) : AttributedProductionRule(rule) {
    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        if(attrs.size() != 1){
            return Pair(false, listOf())
        }
        return canMakeProgramWithAttribute(attrs.toList()[0])
    }

    abstract fun makeChildrenForAttribute(
        attr: NodeAttribute,
        nodesThatFit: List<GenericGrammarNode>
    ): List<GenericGrammarNode>
    abstract fun canMakeProgramWithAttribute(attr: NodeAttribute) : Pair<Boolean, List<List<RuleConstraint>>>
}