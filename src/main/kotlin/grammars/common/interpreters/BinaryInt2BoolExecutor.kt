package grammars.common.interpreters

class BinaryInt2BoolExecutor(val op : Operation, val intType : String, val boolType : String) : BasicFunctionExecutor(listOf(intType, intType), boolType) {
    enum class Operation {
        LT,
        GT,
    }

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val b1 = castToType<Int>(args[0], intType)
        val b2 = castToType<Int>(args[1], intType)
        when(op) {
            Operation.LT -> return b1 < b2
            Operation.GT -> return b1 > b2
        }
    }
}