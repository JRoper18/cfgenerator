package grammars.common.interpreters

import grammar.GenericGrammarNode

class BinaryInt2IntExecutor(val op : Operation, val intType : String) : BasicFunctionExecutor(listOf(intType, intType), intType) {
    enum class Operation {
        PLUS,
        MINUS,
    }

    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val b1 = castToType<Int>(args[0], intType)
        val b2 = castToType<Int>(args[1], intType)
        when(op) {
            Operation.PLUS -> return b1 + b2
            Operation.MINUS -> return b1 - b2
        }
    }
}