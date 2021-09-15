package grammars.deepcoder

import grammar.*
import grammars.common.ListProductionRule
import grammars.common.LowercaseASCIISymbol

val STMT = NtSym("Stmt")
val STMT_LIST = NtSym("StmtList")
val FUNCTION_NAME = NtSym("FuncName")
val FUNCTION_ARGS = NtSym("FuncArgs")
val FUNCTION_ARG = NtSym("FuncArg")
val deepCoderGrammar = AttributeGrammar(listOf(
    APR(ListProductionRule(STMT_LIST, STMT, "\n")),
    APR(ListProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")),
    APR(PR(STMT, listOf( // A statement is just a function call going into a variable.
        LowercaseASCIISymbol,
        StringSymbol(":="),
        FUNCTION_NAME,
        StringSymbol("("),
        FUNCTION_ARGS,
        StringSymbol(")"),
    ))),
    APR(PR(FUNCTION_NAME, listOf(
        StringsetSymbol(listOf(
            "Head",
            "Last",
            "Take",
            "Drop",
        )),
    ))),
))



