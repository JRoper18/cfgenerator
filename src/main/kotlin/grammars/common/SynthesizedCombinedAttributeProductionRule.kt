package grammars.common

import grammar.AttributedProductionRule
import grammar.GenericGrammarNode
import grammar.NodeAttributes
import grammar.constraints.RuleConstraint

/**
 * Because synthesized attribute rules are so simple, we can add them to any other APR without consequence.
 */
class SynthesizedCombinedAttributeProductionRule(val synthesisRules : List<SynthesizeAttributeProductionRule>, val otherRule: AttributedProductionRule) : AttributedProductionRule(otherRule.rule) {
    val rules = synthesisRules + otherRule

    constructor(synthesisRule : SynthesizeAttributeProductionRule, otherRule: AttributedProductionRule) : this(listOf(synthesisRule), otherRule)

    init {
        synthesisRules.forEach { rule ->
            require(rule.rule.equals(this.rule)) {
                "All base synthesis rules must be the same! Rules ${rule.rule} and ${this.rule} don't match. "
            }
        }
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val cons : MutableList<MutableList<RuleConstraint>> = mutableListOf();
        this.rule.rhs.forEach {
            cons.add(mutableListOf()) //Prefill the lists.
        }
        return Pair(true, cons)
    }

    /**
     *
     */
    override fun makeChildrenForAttributes(
        attrs: NodeAttributes,
        nodesThatFit: List<GenericGrammarNode>
    ): List<GenericGrammarNode> {
        val children = otherRule.makeChildrenForAttributes(attrs, nodesThatFit)
        return children;
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        var attrs = NodeAttributes()
        for(rule in rules) {
            attrs = attrs.union(rule.makeSynthesizedAttributes(childAttributes))
        }
        return attrs;
    }
}