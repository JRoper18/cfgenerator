package grammars.common.interpreters

import kotlin.reflect.KClass

class ProgramState {
    val classesToTypes = mutableMapOf<KClass<*>, String>()
    val variableTypes = mutableMapOf<String, String>()
    val variables = mutableMapOf<String, TypedProgramVariables<*>>()

    fun <T> getVars(type : String) : TypedProgramVariables<T> {
        val vars = variables.getOrPut(type) {
            TypedProgramVariables<T>()
        }
        return vars as TypedProgramVariables<T>
    }

    fun getType(name : String) : String? {
        return variableTypes[name]
    }
    fun <T> getVar(name : String) : T? {
        val type = getType(name) ?: return null
        return getVars<T>(type)[name]
    }

    // God this is janky
    fun <T> setVar(name : String, type : String, value : T) {
        val existingVarType = variableTypes.getOrPut(name){
            type
        }
        require(variableTypes[name] == type) {
            "Variable already exists as type $existingVarType"
        }
        getVars<T>(type)[name] = value
    }
}