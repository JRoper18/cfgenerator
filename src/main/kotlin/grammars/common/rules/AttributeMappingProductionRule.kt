package grammars.common.rules

import grammar.*
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint
import grammars.common.mappers.SingleAttributeMapper

/**
 * Takes in a production rule and a map from a pair of a node attribute and the child it came from,
 * To a new node attribute.
 */
class AttributeMappingProductionRule(pr: ProductionRule, val childTypeKey : String, val childTypeLocation : Int, val mapper : SingleAttributeMapper) : KeyedAttributesProductionRule(listOf(childTypeKey), pr) {
    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val attrList = attrs.toList()
        if(attrList.isEmpty()){
            return Pair(true, noConstraints)
        }
        if(attrList.size > 1) {
            return cantMakeProgramReturn
        }
        val attr = attrList[0]
        if(attr.first != childTypeKey) {
            return cantMakeProgramReturn
        }
        val neededType = mapper.backward(attr.second)
        if(neededType.isEmpty()) {
            return cantMakeProgramReturn
        }
        val cons = noConstraints.toMutableList()
        cons[childTypeLocation] = listOf(BasicRuleConstraint(NodeAttribute(childTypeKey, neededType.random())))
        return Pair(true, cons.toList())
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val childTypeAttr = (childAttributes[childTypeLocation].getStringAttribute(childTypeKey)) ?: return NodeAttributes()
        return NodeAttributes.fromAttr(NodeAttribute(childTypeKey, mapper.forward(childTypeAttr)))
    }

}