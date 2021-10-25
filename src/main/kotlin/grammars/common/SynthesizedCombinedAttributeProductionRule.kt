package grammars.common

import grammar.AttributedProductionRule
import grammar.GenericGrammarNode
import grammar.NodeAttributes
import grammar.constraints.RuleConstraint

/**
 * Because synthesized attribute rules are so simple, we can add them to any other APR without consequence.
 */
class SynthesizedCombinedAttributeProductionRule(synthesisRules : List<SynthesizeAttributeProductionRule>, val otherRule: AttributedProductionRule) : AttributedProductionRule(otherRule.rule) {
    val rules = synthesisRules + otherRule
    val synthesisRule : SynthesizeAttributeProductionRule
    constructor(synthesisRule : SynthesizeAttributeProductionRule, otherRule: AttributedProductionRule) : this(listOf(synthesisRule), otherRule)

    init {
        val synthMap = mutableMapOf<String, Int>()
        synthesisRules.forEach { rule ->
            require(rule.rule.equals(this.rule)) {
                "All base synthesis rules must be the same! Rules ${rule.rule} and ${this.rule} don't match. "
            }
            rule.toSynthesize.forEach {
                val attrKey = it.key
                require(!synthMap.containsKey(attrKey)) {
                    "Multiple synthesis rules overlap on key $attrKey"
                }
                synthMap[attrKey] = it.value
            }
        }
        synthesisRule = SynthesizeAttributeProductionRule(synthMap, rule)
    }

    //TODO: Test this
    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val cons : MutableList<MutableList<RuleConstraint>> = mutableListOf();
        this.rule.rhs.forEach {
            cons.add(mutableListOf()) //Prefill the lists.
        }
        // Alright, now remove the attributes we synthesize.
        val attrList = attrs.toList()
        val synthedAttrs = attrList.filter {
            synthesisRule.synthesizedKeys.contains(it.first)
        }
        val unsynthedAttrs = attrList - synthedAttrs.toSet()
        val canMakeSynthedAttrs = synthesisRule.canMakeProgramWithAttributes(NodeAttributes.fromList(synthedAttrs))
        if(!canMakeSynthedAttrs.first) {
            return cantMakeProgramReturn
        }
        // Add the constraints to the list.
        canMakeSynthedAttrs.second.forEachIndexed{ cidx, newCons ->
            cons[cidx].addAll(newCons)
        }
        // Now do the same for unsynthed attrs.
        val canMakeOtherAttrs = otherRule.canMakeProgramWithAttributes(NodeAttributes.fromList(unsynthedAttrs))
        if(!canMakeOtherAttrs.first) {
            return cantMakeProgramReturn
        }
        canMakeOtherAttrs.second.forEachIndexed{ cidx, newCons ->
            cons[cidx].addAll(newCons)
        }
        return Pair(true, cons)
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        var attrs = NodeAttributes()
        for(rule in rules) {
            val ruleAttrs = rule.makeSynthesizedAttributes(childAttributes)
            attrs = attrs.union(ruleAttrs)
        }
        return attrs;
    }
}