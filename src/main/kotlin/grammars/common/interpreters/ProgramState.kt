package grammars.common.interpreters

import kotlin.reflect.KClass

class ProgramState {
    val variableTypes = mutableMapOf<String, String>()
    val variables = mutableMapOf<String, MutableMap<String, Any>>()

    fun getVars(type : String) : MutableMap<String, Any> {
        val vars = variables.getOrPut(type) {
            mutableMapOf()
        }
        return vars
    }

    fun getType(name : String) : String? {
        return variableTypes[name]
    }
    fun getVar(name : String) : Any? {
        val type = getType(name) ?: return null
        return getVars(type)[name]
    }

    // God this is janky
    fun setVar(name : String, type : String, value : Any) {
        val existingVarType = variableTypes.getOrPut(name){
            type
        }
        require(variableTypes[name] == type) {
            "Variable already exists as type $existingVarType"
        }
        getVars(type)[name] = value
    }
    fun unsetVar(name : String) {
        val existingVarType = variableTypes[name]
        variables[existingVarType]!!.remove(name)
        variableTypes.remove(name)
    }
}