package grammars.common.rules

import grammar.*
import grammar.constraints.RuleConstraint

class StringsetSymbolRule(val stringSetSymbol : StringsetSymbol, val stringSymbol : StringSymbol) : AttributedProductionRule(PR(stringSetSymbol, listOf(stringSymbol))) {
    val generatedAttrs = stringSetSymbol.stringAttributeSet.getOrDefault(stringSymbol.name, setOf())
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val map = mutableMapOf(stringSetSymbol.attributeName to stringSymbol.name)
        stringSetSymbol.stringAttributeSet.getOrDefault(stringSymbol.name, setOf()).forEach {
            map[it.first] = it.second
        }
        return NodeAttributes(map)
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        for(attr in attrs.toList()){
            if(attr.first == stringSetSymbol.attributeName) {
                if(attr.second != stringSymbol.name) {
                    return cantMakeProgramReturn
                }
            }
            else if(!generatedAttrs.contains(attr)) {
                return cantMakeProgramReturn
            }
        }
        return Pair(true, noConstraints)
    }
}