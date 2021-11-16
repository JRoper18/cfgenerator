package grammars.common.interpreters

import grammars.common.mappers.SingleAttributeMapper

class IndexIntoFunction(val listType : String, val intType : String, listTypeMapper : SingleAttributeMapper) : TypeMutatingFunctionExecutor(listOf(listType, intType), listTypeMapper.inverse(), 0) {
    override fun execute(args: List<Any>): Any {
        val list = castToType<List<Any>>(args[0], listType)
        val idx = castToType<Int>(args[1], intType)
        return list[idx]
    }
}