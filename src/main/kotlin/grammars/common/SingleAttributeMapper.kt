package grammars.common

interface SingleAttributeMapper {
    /**
     * Takes an attribute val and maps it to another attribute val.
     */
    fun forward(attrVal : String) : String

    /**
     * Takes an attribute value and returns what maps to it.
     */
    fun backward(attrVal : String) : List<String>

    fun inverse() : SingleAttributeMapper {
        return InverseAttributeMapper(this)
    }
}