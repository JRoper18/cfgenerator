package grammars.deepcoder

import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.GrammarNode
import grammar.RootGrammarNode
import grammars.common.SizedListAttributeProductionRule
import java.lang.Exception
import kotlin.random.Random

class DeepCoderInterpreter {
    internal class ParseError : Exception("ParseError for DeepCoder")
    val intVars = mutableMapOf<String, Int>()
    val listVars = mutableMapOf<String, List<Int>>()

    val stringifier = ProgramStringifier()

    val intFunctions : Map<String, (strs : List<String>) -> Int> = mapOf(
        "Head" to {strs ->
            getList(strs[0]).get(0)
        },
        "Last" to { strs ->
            getList(strs[0]).last()
        },
        "Access" to { strs ->
            getList(strs[1])[getInt(strs[0])]
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
        }
    )
    val listFunctions : Map<String, (strs : List<String>) -> List<Int>> = mapOf(
        "Take" to { strs ->
            getList(strs[1]).subList(0, getInt(strs[0]))
        },
        "Drop" to { strs ->
            val l = getList(strs[1])
            l.subList(getInt(strs[0]), l.size)
        },
        "Reverse" to { strs ->
            val l = getList(strs[0])
            l.reversed()
        },
        "Sort" to { strs ->
            val l = getList(strs[0])
            l.sorted()
        }
    )

    fun getList(varname : String) : List<Int> {
        return listVars[varname] ?: throw ParseError();
    }
    fun getInt(varname : String) : Int{
        return intVars[varname] ?: throw ParseError();
    }
    fun interp(program: GenericGrammarNode){
        require(program.lhsSymbol() == STMT_LIST)
        if(program.rhs.size < 2){
            //It's a terminal list.
            return;
        }
        val list = program.rhs[0]
        val stmt = program.rhs[2]
        interp(list)
        interpStmt(stmt)
    }

    fun interpStmt(stmt: GenericGrammarNode) {
        require(stmt.lhsSymbol() == STMT)
        val attrs = stmt.attributes()
        val varname = attrs.getStringAttribute("chosenSymbol")!!
        val vardefStmt = stmt.rhs[2]
        val vardefAttrs = vardefStmt.attributes()
        val varType = vardefAttrs.getStringAttribute(typeNameAttr)

        if(vardefStmt.productionRule == FUNCTION_CALL_RULE) {
            val funcName = vardefAttrs.getStringAttribute("functionName")
            val numFuncArgs = vardefAttrs.getStringAttribute("length")
            val funcArgsNode = vardefStmt.rhs[1]
            check(funcArgsNode.productionRule is SizedListAttributeProductionRule)
            val funcVarInputs = (funcArgsNode.productionRule as SizedListAttributeProductionRule).unroll(funcArgsNode).map {
                it.rhs[0].rhs[0].lhsSymbol().name
            }
            if(varType == intType) {
                intVars[varname] = intFunctions[funcName]!!(funcVarInputs)
            }
            if(varType == listType) {
                listVars[varname] = listFunctions[funcName]!!(funcVarInputs)
            }
        }
        else if(vardefStmt.productionRule == TYPEVAR_RULE) {
            println("INPUT RULE")
            println(varType)
            // Just generate some input.
            // TODO: Seperate this out or take inputs or something, this call means that it's an input variable.
            if(varType == intType) {
                intVars[varname] = Random.nextInt() % 10
            }
            if(varType == listType) {
                val listSize = Random.nextInt() % 10
                val list = mutableListOf<Int>()
                for(i in 0..listSize + 1) {
                    list.add(Random.nextInt() % 20)
                }
                listVars[varname] = list.toList()
            }
        }
        else {
            println(vardefStmt)
            println("WTF")
        }
    }
}
