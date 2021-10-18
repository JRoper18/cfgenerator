package grammars.common

import grammar.*
import grammar.constraints.RuleConstraint

/**
 * Takes in a production rule and a map from a pair of a node attribute and the child it came from,
 * To a new node attribute.
 */
class AttributeMappingProductionRule(pr: ProductionRule, val attributeMapping : Map<Pair<NodeAttribute, Int>, NodeAttribute> = mapOf()) : AttributedProductionRule(pr) {
    /**
     * A map from node attribute you'd want to generate, and a list of node attributes + child locations they'd come from.
     */
    val inverseMap : Map<NodeAttribute, List<Pair<NodeAttribute, Int>>> by lazy {
        val map = mutableMapOf<NodeAttribute, List<Pair<NodeAttribute, Int>>>()
        attributeMapping.forEach {
            map.putIfAbsent(it.value, mutableListOf())
            (map[it.value]!! as MutableList).add(it.key)
        }
        map.keys.forEach {
            map[it] = map[it]!!.toList()
        }
        map.toMap()
    }
    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs = NodeAttributes()
        childAttributes.forEachIndexed { index, childAttrs ->
            childAttrs.toList().forEach {
                val mapped = attributeMapping[Pair(it, index)]
                if(mapped != null){
                    attrs.setAttribute(mapped.first, mapped.second)
                }
            }
        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val neededAttrsSparse = attrs.toList().flatMap {
            inverseMap[it] ?: noConstraints
        }
        //TODO: This
        return super.canMakeProgramWithAttributes(attrs)
    }

}