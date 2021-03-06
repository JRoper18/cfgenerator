package interpreters.common.executors

class EqualsExecutor() : BasicFunctionExecutor(listOf(anyType, anyType), anyType) {

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        return args[0] == args[1]
    }
}