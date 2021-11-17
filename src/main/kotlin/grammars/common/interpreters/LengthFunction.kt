package grammars.common.interpreters

class LengthFunction(val listType : String, intType : String) : BasicFunctionExecutor(listOf(listType), intType) {
    override fun execute(args: List<Any>): Any {
        return castToType<List<Any>>(args[0], listType).size
    }
}