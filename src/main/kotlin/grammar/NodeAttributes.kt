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

    fun copyAttribute(key : String, into : NodeAttributes) {
        val stringAttr = getStringAttribute(key)
        if(stringAttr != null) {
            into.setAttribute(key, stringAttr)
            return
        }
        throw NullPointerException()
    }

    fun union(other: NodeAttributes) : NodeAttributes{
        val new = this.copy()
        new.stringAttributes.putAll(other.stringAttributes)
        require(new.stringAttributes.size == other.stringAttributes.size + this.stringAttributes.size) // No duplicated/overridden attributes.
        return new
    }
    fun isEmpty() : Boolean {
        return stringAttributes.isEmpty()
    }

}