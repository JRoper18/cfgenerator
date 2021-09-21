package grammar

import java.lang.NullPointerException

data class NodeAttributes(private val intAttributes: MutableMap<String, Int> = mutableMapOf(),
                     private val stringAttributes: MutableMap<String, String> = mutableMapOf()){
    fun setAttribute(key : String, value : Int) {
        require(!stringAttributes.containsKey(key))
        intAttributes[key] = value;
    }
    fun setAttribute(key : String, value : String) {
        require(!intAttributes.containsKey(key))
        stringAttributes[key] = value;
    }
    fun getIntAttribute(key : String) : Int? {
        return intAttributes[key]
    }
    fun getStringAttribute(key : String) : String? {
        return stringAttributes[key]
    }

    fun copyAttribute(key : String, into : NodeAttributes) {
        val intAttr = getIntAttribute(key)
        if(intAttr != null) {
            into.setAttribute(key, intAttr)
            return
        }
        val stringAttr = getStringAttribute(key)
        if(stringAttr != null) {
            into.setAttribute(key, stringAttr)
            return
        }
        throw NullPointerException()
    }

    fun union(other: NodeAttributes) : NodeAttributes{
        val new = this.copy()
        new.intAttributes.putAll(other.intAttributes)
        require(new.intAttributes.size == other.intAttributes.size + this.intAttributes.size) // No duplicated attributes.
        new.stringAttributes.putAll(other.stringAttributes)
        require(new.stringAttributes.size == other.stringAttributes.size + this.stringAttributes.size) // No duplicated/overridden attributes.
        return new
    }
    fun isEmpty() : Boolean {
        return intAttributes.isEmpty() && stringAttributes.isEmpty()
    }

}