package grammars.common.interpreters

import grammar.GenericGrammarNode
import grammars.common.mappers.IdentityMapper

class FilterFunction(val listType : String, val lambdaType : String) : MutatingHigherOrderExecutor(listOf(anyType), listOf(listType), 1, IdentityMapper()) {
    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val lambdaProgNode = castToType<GenericGrammarNode>(args[0], lambdaType)
        val inList = castToType<List<Any>>(args[1], listType)
        return inList.filter {
            interpreter(lambdaProgNode, listOf(it)) as Boolean
        }
    }
}