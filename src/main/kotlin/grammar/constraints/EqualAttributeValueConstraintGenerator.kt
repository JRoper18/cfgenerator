package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import kotlin.random.Random

class EqualAttributeValueConstraintGenerator(val attrKeys : Set<String>, val possibleValues : Set<String>): ConstraintGenerator {
    override fun generate(attrs: NodeAttributes, random: Random): List<RuleConstraint> {
        var attrVal : String = ""
        var setVal = false
        for(key in attrKeys) {
            val tmp = attrs.getStringAttribute(key)
            if(tmp != null){
                setVal = true
                attrVal = tmp
                break
            }
        }
        if(!setVal) {
            // Shit, no attributes existed with those keys. Pick one from the possible values
            val attrVal = possibleValues.random(random)
            return attrKeys.map {
                BasicRuleConstraint(NodeAttribute(it, attrVal))
            }
        }
        return attrKeys.map {
            BasicRuleConstraint(NodeAttribute(it, attrVal))
        }
    }
}