package grammars.common.mappers

class IdentityMapper : SingleAttributeMapper {
    override fun forward(attrVal: String): String {
        return attrVal
    }

    override fun backward(attrVal: String): List<String> {
        return listOf(attrVal)
    }

}