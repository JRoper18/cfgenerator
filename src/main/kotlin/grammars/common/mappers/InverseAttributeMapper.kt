package grammars.common.mappers

class InverseAttributeMapper(val orig : SingleAttributeMapper, val default : String = "null") : SingleAttributeMapper {
    override fun forward(attrVal: String): String {
        val possible =  orig.backward(attrVal)
        if(possible.isEmpty()) {
            return default
        }
        return possible.get(0)
    }

    override fun backward(attrVal: String): List<String> {
        return listOf(orig.forward(attrVal))
    }

}