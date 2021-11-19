package interpreters.common.executors

class NegationExecutor(val boolType : String) : BasicFunctionExecutor(listOf(boolType), boolType) {

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val b1 = castToType<Boolean>(args[0], boolType)
        return !b1
    }
}