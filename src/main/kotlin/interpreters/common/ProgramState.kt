package interpreters.common

data class ProgramState(
    val variableTypes: MutableMap<String, String> = mutableMapOf() ,
    val variables: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()
){

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
        if(variables[existingVarType]!!.isEmpty()) {
            variables.remove(existingVarType)
        }
        variableTypes.remove(name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProgramState

        if (variableTypes != other.variableTypes) return false
        if (variables != other.variables) return false

        return true
    }

}