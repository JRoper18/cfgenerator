package grammars.common.interpreters

abstract class FunctionExecutor(val inTypes : List<String>, val outType : String) {
    val numArgs = inTypes.size
    inline fun <reified T> castToType(arg : Any, wantedType : String) : T {
        if(!(arg is T)) {
            throw TypedFunctionalInterpreter.TypeError(wantedType = wantedType)
        }
        return arg
    }

    fun checkArgs(args : List<Any>) {
        if(args.size != numArgs){
            throw TypedFunctionalInterpreter.ParseError("Function expects ${inTypes.size} args")
        }
        args.forEachIndexed { index, any ->
            castToType(args[index], inTypes[index])
        }
    }
    abstract fun execute(args : List<Any>) : Any
}