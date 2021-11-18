package interpreters.common

class ContainsExecutor(val listType : String, val boolType : String) : BasicFunctionExecutor(
    inTypes = listOf(listType, anyType),
    outType = boolType
) {
    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val typedList = castToType<List<Any>>(args[0], listType)
        return args[1] in typedList
    }
}