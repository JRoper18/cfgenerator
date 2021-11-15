package grammars.common.rules

import grammar.*
import grammar.constraints.RuleConstraint

class InitAttributeProductionRule(rule: ProductionRule, val initialKey : String, val initialVal : String) : KeyedAttributesProductionRule(listOf(initialKey), rule) {
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val ret = NodeAttributes()
        ret.setAttribute(initialKey, initialVal)
        return ret
    }
    override fun canMakeProgramWithAttributes(attrs: NodeAttributes) : Pair<Boolean, List<List<RuleConstraint>>> {
        if(attrs.size() > 1) {
            return cantMakeProgramReturn
        }
        if(attrs.size() == 0){
            return Pair(true, noConstraints)
        }
        val attr = attrs.toList()[0]
        return Pair(attr.first == initialKey && attr.second == initialVal, noConstraints)
    }
}