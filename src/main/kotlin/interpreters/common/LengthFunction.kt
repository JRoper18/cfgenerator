package interpreters.common

class LengthFunction(val listType : String, intType : String) : BasicFunctionExecutor(listOf(listType), intType) {
    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        return castToType<List<Any>>(args[0], listType).size
    }
}