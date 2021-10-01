package grammars.deepcoder

import kotlin.random.Random

data class DeepCoderVariables(val intVars: MutableMap<String, Int> = mutableMapOf(), val listVars: MutableMap<String, List<Int>> = mutableMapOf()) {
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
                    intType -> vars.generateIntVar(varname)
                    listType -> vars.generateListVar(varname)
                }
            }
            return vars
        }
    }
}