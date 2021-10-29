package grammars.common

import grammar.AttributedProductionRule
import grammar.NodeAttributes
import grammar.constraints.RuleConstraint
import utils.duplicates

class CombinedKeyedAttributesRule(val rules : List<KeyedAttributesProductionRule>) : KeyedAttributesProductionRule(rules.flatMap {
   it.attrKeysMade
}, rules.first().rule) {
    init {
        rules.forEach { rule ->
            require(rule.rule.equals(this.rule)) {
                "All base synthesis rules must be the same! Rules ${rule.rule} and ${this.rule} don't match. "
            }
        }
        val dups = this.attrKeysMade.duplicates()
        require(dups.isEmpty()) {
            "Duplicate attribute keys $dups"
        }
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val cons : MutableList<MutableList<RuleConstraint>> = mutableListOf();
        this.rule.rhs.forEach {
            cons.add(mutableListOf()) //Prefill the lists.
        }
        val attrList = attrs.toList()
        // Alright, now remove the attributes we synthesize.
        this.rules.forEach { subrule  ->
            val synthedAttrs = attrList.filter {
                subrule.attrKeysMade.contains(it.first)
            }
            val canMakeSynthedAttrs = subrule.canMakeProgramWithAttributes(NodeAttributes.fromList(synthedAttrs))
            if(!canMakeSynthedAttrs.first) {
                return cantMakeProgramReturn
            }
            // Add the constraints to the list.
            canMakeSynthedAttrs.second.forEachIndexed{ cidx, newCons ->
                cons[cidx].addAll(newCons)
            }
        }
        return Pair(true, cons)
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        var attrs = NodeAttributes()
        for(subrule in rules) {
            val ruleAttrs = subrule.makeSynthesizedAttributes(childAttributes)
            attrs = attrs.union(ruleAttrs)
        }
        return attrs;
    }
}