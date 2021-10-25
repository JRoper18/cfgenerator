package grammar.constraints

import grammar.NodeAttribute
import grammar.NodeAttributes
import kotlin.random.Random

class OrRuleConstraint(val constraints : Collection<RuleConstraint>) : RuleConstraint {
    override fun satisfies(attrs: NodeAttributes): Boolean {
        for(cons in constraints) {
            if(cons.satisfies(attrs)) {
                return true
            }
        }
        return false
    }

    override fun makeSatisfyingAttribute(random: Random): NodeAttribute {
        return constraints.random(random).makeSatisfyingAttribute(random)
    }
}