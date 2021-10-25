import grammar.AttributedProductionRule
import grammar.NodeAttribute
import grammar.NodeAttributes
import grammar.ProductionRule
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.RuleConstraint
import grammars.common.SynthesizedCombinedAttributeProductionRule
import java.text.ParseException

class OrderedSynthesizedAttributeRule(val toSynthesize: Set<Pair<String, Int>>, rule: ProductionRule) : AttributedProductionRule(rule) {

    val synthesizedKeys : Map<String, List<Int>> by lazy {
        val mmap = mutableMapOf<String, MutableList<Int>>()
        toSynthesize.forEach {
            val key = makeAttrKey(it.first, it.second)
            mmap.getOrDefault(key, mutableListOf<Int>())
            mmap[key]!!.add(it.second)
        }
        mmap
    }

    companion object {
        fun makeAttrKey(childAttrKey : String, childIdx : Int) : String {
            return ("${childIdx}.${childAttrKey}")
        }
        fun attrKeyToPair(key : String) : Pair<String, Int>? {
            val split = key.split(".")
            try {
                return Pair(split[1], split[0].toInt())
            } catch (ex : IndexOutOfBoundsException) {
                return null
            } catch (ex2 : ParseException) {
                return null
            }
            return null
        }
        fun isOrderedAttrKey(key : String) : Boolean {

            return true
        }
    }

    init {
        toSynthesize.forEach {
            require(it.second < rule.rhs.size) {
                "The index of the child to synthesize from must be within the bounds of the rule's RHS  "
            }
        }
    }

    override fun makeSynthesizedAttributes(childAttributes: List<NodeAttributes>): NodeAttributes {
        val attrs = NodeAttributes()
        for(pair in toSynthesize){
            val key = pair.first
            val cAttrVal = childAttributes[pair.second].getStringAttribute(key) ?: continue
            attrs.setAttribute(makeAttrKey(key, pair.second), cAttrVal)

        }
        return attrs
    }

    override fun canMakeProgramWithAttributes(attrs: NodeAttributes): Pair<Boolean, List<List<RuleConstraint>>> {
        val constraintList = rule.rhs.map {
            mutableListOf<RuleConstraint>()
        }.toMutableList()
        attrs.toList().forEach { attr ->
            val pair = attrKeyToPair(attr.first) ?: return cantMakeProgramReturn // If it's a pair we generate, make a constraint.
            // Else it's null, and we can't make it.
            constraintList[pair.second].add(BasicRuleConstraint(NodeAttribute(pair.first, attr.second)))
        }
        return Pair(true, constraintList.map {
            it.toList()
        }.toList())
    }
}