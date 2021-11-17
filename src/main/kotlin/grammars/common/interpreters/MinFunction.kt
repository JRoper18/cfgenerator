package grammars.common.interpreters

import grammar.GenericGrammarNode

class MinFunction(intType : String, val intListType : String) : BasicFunctionExecutor(listOf(intListType), intType) {
    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val typedList = castToType<List<Int>>(args[0], intListType)
        return typedList.maxOrNull()!!
    }
}