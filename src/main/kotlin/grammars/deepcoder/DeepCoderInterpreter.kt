package grammars.deepcoder

import grammar.GenericGrammarNode
import grammars.common.SizedListAttributeProductionRule
import java.lang.Exception
import kotlin.random.Random

class DeepCoderInterpreter(val variables : DeepCoderVariables = DeepCoderVariables()) {
    internal class ParseError : Exception("ParseError for DeepCoder")

    val intFunctions : Map<String, (strs : List<String>) -> Int> = mapOf(
        "Head" to {strs ->
            val l = getList(strs[0])
            l[0]
        },
        "Last" to { strs ->
            getList(strs[0]).last()
        },
        "Access" to { strs ->
            val l = getList(strs[1])
            val idx = getInt(strs[0])
            checkIsInList(l, idx)
            l[idx]
        },
        "Minimum" to { strs ->
            getList(strs[0]).minOrNull() ?: throw ParseError()
        },
        "Maximum" to { strs ->
            getList(strs[0]).maxOrNull() ?: throw ParseError()
        },
        "Sum" to { strs ->
            val l = getList(strs[0])
            l.sum()
        },
        "Count" to { strs ->
            val l = getList(strs[1])
            val f = intToBoolLambdas[strs[0]] ?: throw ParseError()
            l.count {
                f(it)
            }
        }
    )
    val listFunctions : Map<String, (strs : List<String>) -> List<Int>> = mapOf(
        "Take" to { strs ->
            val l = getList(strs[1])
            val idx = getInt(strs[0])
            checkIsInList(l, idx)
            l.subList(0, idx)
        },
        "Drop" to { strs ->
            val l = getList(strs[1])
            val idx = getInt(strs[0])
            checkIsInList(l, idx)
            l.subList(idx, l.size)
        },
        "Reverse" to { strs ->
            val l = getList(strs[0])
            l.reversed()
        },
        "Sort" to { strs ->
            val l = getList(strs[0])
            l.sorted()
        },
        "Map" to { strs ->
            val l = getList(strs[1])
            val f = intToIntLambdas[strs[0]] ?: throw ParseError()
            l.map {
                f(it)
            }
        },
        "Filter" to { strs ->
            val l = getList(strs[1])
            val f = intToBoolLambdas[strs[0]] ?: throw ParseError()
            l.filter {
                f(it)
            }
        },
        "ZipWith" to { strs ->
            val l1 = getList(strs[1])
            val l2 = getList(strs[2])
            val f = zipLambdas[strs[0]] ?: throw ParseError()
            l1.zip(l2) { a, b ->
                f(a, b)
            }
        },
        "ScanL1" to { strs ->
            val l = getList(strs[1])
            val nl = mutableListOf<Int>()
            val f = zipLambdas[strs[0]] ?: throw ParseError()
            nl.set(0, l[0])
            for(i in 1 until l.size){
                nl[i] = f(nl[i - 1], l[i])
            }
            nl.toList()
        }

    )

    val intToIntLambdas : Map<String, (input: Int) -> Int> = mapOf(
        "(+1)" to { it + 1 },
        "(-1)" to { it - 1 },
        "(*2)" to { it * 2 },
        "(/2)" to { it / 2 },
        "(*(-1))" to { -it },
        "(**2)" to { it * it },
        "(*3)" to { it * 3 },
        "(/3)" to { it / 3 },
        "(*4)" to { it * 4 },
        "(/4)" to { it / 4 },
    )
    val intToBoolLambdas : Map<String, (input : Int) -> Boolean> = mapOf(
        "(<0)" to { it < 0 },
        "(>0)" to { it > 0 },
        "(%2==0)" to { it%2 == 0 },
        "(%2==1)" to { it%2==1 },
    )
    val zipLambdas : Map<String, (in1 : Int, in2 : Int) -> Int> = mapOf(
        "(+)" to { in1, in2 ->
            in1 + in2
        },
        "(-)" to { in1, in2 ->
            in1 - in2
        },
        "(*)" to { in1, in2 ->
            in1 * in2
        },
        "MIN" to { in1, in2 ->
            minOf(in1, in2)
        },
        "MAX" to { in1, in2 ->
            maxOf(in1, in2)
        },
    )

    private fun checkIsInList(l : List<Int>, idx : Int) {
        if(idx >= l.size || idx < 0){
            throw ParseError()
        }
    }
    private fun getList(varname : String) : List<Int> {
        return variables.listVars[varname] ?: throw ParseError();
    }
    private fun getInt(varname : String) : Int{
        return variables.intVars[varname] ?: throw ParseError();
    }

    companion object {
        /**
         * Returns a map from input variable names to their types.
         */
        fun getInputs(program: GenericGrammarNode) : Map<String, String> {
            val map = mutableMapOf<String, String>()
            program.forEachInTree {
                if(it.lhsSymbol() == STMT){
                    val vardefStmt = it.rhs[2]
                    if (vardefStmt.productionRule == TYPEVAR_RULE) {
                        // This means it's not just ANY variable definition: It's a NEW/input variable definition. 
                        val attrs = it.attributes()
                        val varname = attrs.getStringAttribute(varAttrName)!!
                        val vardefAttrs = vardefStmt.attributes()
                        val varType = vardefAttrs.getStringAttribute(typeNameAttr) ?: throw ParseError()
                        map[varname] = varType
                    }
                }
            }
            return map
        }
    }
    fun interp(program: GenericGrammarNode) : String {
        require(program.lhsSymbol() == STMT_LIST)
        if(program.rhs.size < 2){
            //It's a terminal list.
            return "Null";
        }
        val list = program.rhs[0]
        val stmt = program.rhs[2]
        interp(list)
        return interpStmt(stmt)
    }

    /**
     * Interprets a single statement in deepcoder, which always assigns a variable.
     * Returns a string representation of the value of the variable just assigned.
     */
    fun interpStmt(stmt: GenericGrammarNode) : String {
        require(stmt.lhsSymbol() == STMT)
        val attrs = stmt.attributes()
        val varname = attrs.getStringAttribute(varAttrName)!!
        val vardefStmt = stmt.rhs[2]
        val vardefAttrs = vardefStmt.attributes()
        val varType = vardefAttrs.getStringAttribute(typeNameAttr)

        if(vardefStmt.productionRule == FUNCTION_CALL_RULE) {
            if(variables.hasVar(varname)){ //Can't re-declare vars.
                throw ParseError()
            }
            val funcName = vardefAttrs.getStringAttribute(functionNameAttr)
            val numFuncArgs = vardefAttrs.getStringAttribute("length")
            val funcArgsNode = vardefStmt.rhs[1]
            check(funcArgsNode.productionRule is SizedListAttributeProductionRule)
            val funcVarInputs = (funcArgsNode.productionRule as SizedListAttributeProductionRule).unroll(funcArgsNode).map {
                it.rhs[0].rhs[0].lhsSymbol().name
            }
            if(varType == intType) {
                val ret = intFunctions[funcName]!!(funcVarInputs)
                variables.intVars[varname] = ret
                return ret.toString()
            }
            if(varType == listType) {
                val ret = listFunctions[funcName]!!(funcVarInputs)
                variables.listVars[varname] = ret
                return ret.toString()
            }
        }
        else if(vardefStmt.productionRule == TYPEVAR_RULE) {
            // An input var. Hope we've specified something for it...
        }
        else {
            println(vardefStmt)
            println("WTF")
        }
        return "Null"
    }
}
