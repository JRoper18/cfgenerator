package grammars.common.interpreters

class MinFunction(intType : String, val intListType : String) : BasicFunctionExecutor(listOf(intListType), intType) {
    override fun execute(args: List<Any>): Any {
        val typedList = castToType<List<Int>>(args[0], intListType)
        return typedList.maxOrNull()!!
    }
}