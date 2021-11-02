package grammars.common

class WrapperAttributeMapper : SingleAttributeMapper {
    override fun forward(attrVal: String): String {
        return "[$attrVal]"
    }

    override fun backward(attrVal: String): List<String> {
        if(attrVal.first() != '[' || attrVal.last() != ']'){
            return listOf()
        }
        return listOf(attrVal.substring(1, attrVal.length - 1))
    }
}