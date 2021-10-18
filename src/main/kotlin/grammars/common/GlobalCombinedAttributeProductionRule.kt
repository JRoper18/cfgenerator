package grammars.common

import grammar.AttributedProductionRule
import grammar.GenericGrammarNode
import grammar.NodeAttributes
import grammar.constraints.RuleConstraint

/**
 * This rule takes all attributes with keys that match the given Regexes and moves them up and down the tree.
 * So, if you declare a variable attribute, and you have a regex to detect it,
 * Then when that rule is produced, the node, it's rightmost siblings, and it's paretns with right siblings
 * Will all have that attribute
 */
class GlobalCombinedAttributeProductionRule(val attrKeyRegexes : Set<Regex>, val combinedWith : AttributedProductionRule) : AttributedProductionRule(combinedWith.rule) {
    override fun makeInheritedAttributes(
        myIdx: Int,
        parentAttributes: NodeAttributes,
        siblingAttributes: List<NodeAttributes>
    ): NodeAttributes {
        var attrs = combinedWith.makeInheritedAttributes(myIdx, parentAttributes, siblingAttributes)

        (siblingAttributes.subList(0, myIdx) + listOf(parentAttributes)).forEach { attr ->
            attrKeyRegexes.forEach {
                val filteredAttrs = attr.filterRegex(it)
                attrs = attrs.union(filteredAttrs)
            }
        }
        return attrs
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        var attrs = combinedWith.makeSynthesizedAttributes(childAttributes)
        (childAttributes).forEach { attr ->
            attrKeyRegexes.forEach {
                val filteredAttrs = attr.filterRegex(it)
                attrs = attrs.union(filteredAttrs)
            }
        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        return combinedWith.canMakeProgramWithAttributes(attrs)
    }

}