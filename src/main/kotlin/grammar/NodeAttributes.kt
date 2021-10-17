package grammar

import java.lang.NullPointerException

data class NodeAttributes(
                     private val stringAttributes: MutableMap<String, String> = mutableMapOf()){
    fun setAttribute(key : String, value : String) {
        stringAttributes[key] = value;
    }
    fun getStringAttribute(key : String) : String? {
        return stringAttributes[key]
    }

    fun matchingAttributeKeys(match: Regex) : Collection<String> {
        return this.stringAttributes.keys.filter {
            match.matches(it)
        }
    }

    fun filterRegex(r : Regex) : NodeAttributes {
        return NodeAttributes(this.stringAttributes.filter {
            r.matches(it.key)
        }.toMutableMap())
    }

    fun copyAttributeIfPresent(key: String, into: NodeAttributes) {
        val stringAttr = getStringAttribute(key)
        if(stringAttr != null) {
            into.setAttribute(key, stringAttr)
            return
        }
    }
    fun copyAttribute(key : String, into : NodeAttributes) {
        if(!stringAttributes.containsKey(key)){
            throw NullPointerException()
        }
        copyAttributeIfPresent(key, into)
    }

    fun union(other: NodeAttributes) : NodeAttributes{
        val new = this.copy()
        for(attr in other.stringAttributes) {
            val thisAttrVal = this.getStringAttribute(attr.key)
            if(thisAttrVal != null) {
                require(thisAttrVal == attr.value) {
                    "Attribute with key ${attr.key} must match: ${attr.value} vs. ${thisAttrVal}"
                }
            }
            new.stringAttributes[attr.key] = attr.value
        }
        return new
    }
    fun isEmpty() : Boolean {
        return stringAttributes.isEmpty()
    }

    fun toList() : List<NodeAttribute> {
        return this.stringAttributes.entries.toList().map {
            NodeAttribute(it.key, it.value)
        }
    }
    companion object {
        fun fromList(list : List<NodeAttribute>) : NodeAttributes {
            val map = mutableMapOf<String, String>()
            list.forEach {
                map.put(it.first, it.second)
            }
            return NodeAttributes(map)
        }
        fun fromAttr(attr: NodeAttribute) : NodeAttributes {
            return fromList(listOf(attr))
        }
    }

    fun size() : Int {
        return stringAttributes.size
    }
    override fun toString() : String {
        return this.stringAttributes.toString()
    }

    fun copy() : NodeAttributes {
        return NodeAttributes(stringAttributes.toMutableMap())
    }

}