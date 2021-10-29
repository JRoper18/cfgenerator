package grammars.common

import grammar.APR
import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.PR
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint

/**
 * Attributes for rules that do higher-order transformations on types.
 * For example, if we have an attribute that is "type", and we want to change it to "list<type>"
 */
open class HigherOrderSynthesizedRule(val childTypeKey : String, val childTypeLocation : Int, rule : PR) :
    KeyedAttributesProductionRule(listOf(childTypeKey), rule) {
    open fun wrapType(type : String) : String {
        return "[$type]"
    }

    /**
     * Unwraps a type, list turning a Collection<Int> into <int>.
     * Returns null if not possible.
     */
    open fun unwrapType(type : String) : String? {
        if(type.first() != '[' || type.last() != ']'){
            return null
        }
        return type.substring(1, type.length - 1)
    }

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
        val neededType = unwrapType(attr.second) ?: return cantMakeProgramReturn
        val cons = noConstraints.toMutableList()
        cons[childTypeLocation] = listOf(BasicRuleConstraint(NodeAttribute(childTypeKey, neededType)))
        return Pair(true, cons.toList())
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val childTypeAttr = (childAttributes[childTypeLocation].getStringAttribute(childTypeKey)) ?: return NodeAttributes()
        return NodeAttributes.fromAttr(NodeAttribute(childTypeKey, wrapType(childTypeAttr)))
    }
}