package grammars.common.rules

import grammars.common.mappers.SingleAttributeMapper

class FiniteAttributeMapper(val attrMap : Map<String, String>, val default : String = "null") : SingleAttributeMapper {
    /**
     * A map from node attribute you'd want to generate, and a list of node attributes + child locations they'd come from.
     */
    val inverseMap : Map<String, List<String>> by lazy {
        val map = mutableMapOf<String, List<String>>()
        attrMap.forEach {
            map.putIfAbsent(it.value, mutableListOf())
            (map[it.value]!! as MutableList).add(it.key)
        }
        map.keys.forEach {
            map[it] = map[it]!!.toList()
        }
        map.toMap()
    }

    override fun forward(attrVal: String): String {
        return attrMap[attrVal] ?: default
    }

    override fun backward(attrVal: String): List<String> {
        return inverseMap[attrVal] ?: listOf()
    }
}