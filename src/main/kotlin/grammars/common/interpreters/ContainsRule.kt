package grammars.common.interpreters

import grammar.GenericGrammarNode
import grammars.common.mappers.IdentityMapper

class ContainsRule(val listType : String, val boolType : String) : BasicFunctionExecutor(
    inTypes = listOf(listType, anyType),
    outType = boolType
) {
    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val typedList = castToType<List<Any>>(args[0], listType)
        return args[1] in typedList
    }
}