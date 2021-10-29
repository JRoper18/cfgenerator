package grammars.common

import grammar.NodeAttributes
import grammar.Symbol

class VariableChildValueRule(lhs : Symbol, rhs : Symbol, varnameAttrKey : String, suffix : String, val childAttrKey : String):
    VariableAttributeRule(lhs, rhs, varnameAttrKey, suffix) {
    override fun makeAttrValueFromChildren(childAttributes: NodeAttributes, attrKey: String): String {
        return childAttributes.getStringAttribute(childAttrKey) ?: "null"
    }
}