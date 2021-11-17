package grammars.common.interpreters

import grammar.GenericGrammarNode

class LengthFunction(val listType : String, intType : String) : BasicFunctionExecutor(listOf(listType), intType) {
    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        return castToType<List<Any>>(args[0], listType).size
    }
}