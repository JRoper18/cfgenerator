package grammar.constraints

import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.NodeAttributes

/**
 * Given an attribute key, get the value of that attribute and see if it matches some lookup table.
 * An example is function name and argument length:
 * If lookupKey functionName == "add", and compareToKey "argLength" == "2",
 * Then we return true if table["add"] == "2"
// */
class LookupConstraintGenerator(val lookupKey : String, val compareToKey : String, val lookupTable: Map<String, String>) : ConstraintGenerator {
    //    override fun generate(at){
//        val attrs = attributes()
//
//    }
    override fun generate(attrs: NodeAttributes): List<RuleConstraint> {
        val lookupVal = attrs.getStringAttribute(lookupKey)
        val compareToVal = attrs.getStringAttribute(compareToKey)
        if(lookupVal == null || compareToVal == null) {
            return listOf(UnsatConstraint());
        }
        return listOf(BasicRuleConstraint(NodeAttribute(compareToKey, compareToVal)))
    }

}