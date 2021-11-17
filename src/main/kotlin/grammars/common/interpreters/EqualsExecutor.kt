package grammars.common.interpreters

import grammar.GenericGrammarNode

class EqualsExecutor() : BasicFunctionExecutor(listOf(anyType), anyType) {

    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        return args[0] == args[1]
    }
}