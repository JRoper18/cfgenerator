package grammars.common.interpreters

class BinaryBool2BoolExecutor(val op : Operation, val boolType : String) : BasicFunctionExecutor(listOf(boolType, boolType), boolType) {
    enum class Operation {
        OR,
        AND
    }

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val b1 = castToType<Boolean>(args[0], boolType)
        val b2 = castToType<Boolean>(args[1], boolType)
        when(op) {
            Operation.OR -> return b1 || b2
            Operation.AND -> return b1 && b2
        }
    }
}