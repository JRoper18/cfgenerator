package grammars.common

import grammar.AttributedProductionRule
import grammar.NodeAttribute
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
        if(attrs.isEmpty()) {
            return Pair(true, noConstraints)
        }
        val cons : MutableList<MutableList<RuleConstraint>> = mutableListOf();
        this.rule.rhs.forEach {
            cons.add(mutableListOf()) //Prefill the lists.
        }
        val attrList = attrs.toList().toMutableSet()
        // Alright, now remove the attributes we synthesize.
        for(subrule in this.rules){
            val synthedAttrs = attrList.filter {
                subrule.attrKeysMade.contains(it.first)
            }
            if(synthedAttrs.isEmpty()) {
                // Ths rule can't be used to make stuff
                continue
            }
            attrList.removeAll(synthedAttrs)
            val canMakeSynthedAttrs = subrule.canMakeProgramWithAttributes(NodeAttributes.fromList(synthedAttrs))
            if(!canMakeSynthedAttrs.first) {
                return cantMakeProgramReturn
            }
            // Add the constraints to the list.
            canMakeSynthedAttrs.second.forEachIndexed{ cidx, newCons ->
                cons[cidx].addAll(newCons)
            }
        }
        if(attrList.isNotEmpty()) {
            return cantMakeProgramReturn
            // Can happen when we don't hit every single attribute with our rules. If there's attributes left over, we're toast.
        }
        return Pair(true, cons.map {
            it.distinct().toList()
        })
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        var attrs = NodeAttributes()
        for(subrule in rules) {
            val ruleAttrs = subrule.makeSynthesizedAttributes(childAttributes)
            attrs = attrs.union(ruleAttrs)
        }
        return attrs;
    }

    fun flatRules() : List<AttributedProductionRule> {
        return rules.flatMap {
            if(it is CombinedKeyedAttributesRule) {
                it.flatRules()
            }
            else {
                listOf(it)
            }
        }
    }
}