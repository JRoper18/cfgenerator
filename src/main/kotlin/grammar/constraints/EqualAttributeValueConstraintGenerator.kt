package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import kotlin.random.Random

class EqualAttributeValueConstraintGenerator(val attrKeys : Set<String>): ConstraintGenerator {
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
            // Shit, no attributes existed with those keys. I guess they're all equal...?
            return listOf()
        }
        return attrKeys.map {
            BasicRuleConstraint(NodeAttribute(it, attrVal))
        }
    }
}