package grammars.deepcoder

import kotlin.random.Random

data class DeepCoderVariables(val intVars: MutableMap<String, Int> = mutableMapOf(), val listVars: MutableMap<String, List<Int>> = mutableMapOf()) {
    constructor(inputStr: String) : this() {
        val decls = inputStr.trim().lines()
        for(decl in decls) {
            val split = decl.split(" = ")
            val varname = split[0]
            val varVal = split[1]
            if(varVal[0] == '[') {
                // It's a list.
                val varListVal = varVal.removeSuffix("]").split(",").map {
                    it.toInt()
                }
                listVars[varname] = varListVal
            } else {
                // It's an int.
                intVars[varname] = varVal.toInt()
            }
        }
    }
    fun generateListVar(name: String) : List<Int> {
        val listSize = Math.abs((Random.nextInt() % 10)) + 1
        val list = mutableListOf<Int>()
        for(i in 0..listSize + 1) {
            list.add(Random.nextInt() % 20)
        }
        listVars[name] = list
        return list
    }
    fun generateIntVar(name: String) : Int{
        val ret = Random.nextInt() % 10
        intVars[name] = ret
        return ret
    }
    fun hasVar(name : String) : Boolean {
        return (intVars[name] ?: listVars[name]) != null
    }
    companion object {
        /**
         * Returns a random variable set given a map from varnames to types.
         */
        fun fromInputs(inputs: Map<String, String>) : DeepCoderVariables{
            val vars = DeepCoderVariables()
            inputs.forEach{
                val varname = it.key
                val vartype = it.value
                when(vartype){
                    DeepCoderGrammar.intType -> vars.generateIntVar(varname)
                    DeepCoderGrammar.listType -> vars.generateListVar(varname)
                }
            }
            return vars
        }
    }

    override fun toString() : String{
        var str = ""
        intVars.forEach {
            str += "${it.key} = ${it.value}\n"
        }
        listVars.forEach {
            str += "${it.key} = ${it.value}\n"
        }
        return str.trim()
    }

    fun copy() : DeepCoderVariables {
        return DeepCoderVariables(intVars.toMutableMap(), listVars.toMutableMap())
    }
}