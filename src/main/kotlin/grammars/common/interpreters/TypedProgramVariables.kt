package grammars.common.interpreters

typealias TypedProgramVariables<V> = MutableMap<String, V>
fun <T> TypedProgramVariables() : TypedProgramVariables<T> {
    return mutableMapOf<String, T>()
}