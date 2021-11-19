package interpreters.common.executors

class MaxFunction(intType : String, val intListType : String) : BasicFunctionExecutor(listOf(intListType), intType) {
    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val typedList = castToType<List<Int>>(args[0], intListType)
        return typedList.maxOrNull()!!
    }
}