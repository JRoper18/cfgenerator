package grammars.deepcoder

import grammar.*
import grammars.common.ListProductionRule
import grammars.common.LowercaseASCIISymbol
import grammars.common.StringSetProductionRules
import grammars.common.TerminalProductionRule

val STMT = NtSym("Stmt")
val STMT_LIST = NtSym("StmtList")
val FUNCTION_NAME = StringsetSymbol(listOf(
    "Head",
    "Last",
    "Take",
    "Drop",
))
val FUNCTION_ARGS = NtSym("FuncArgs")
val FUNCTION_ARG = NtSym("FuncArg")
val deepCoderGrammar = AttributeGrammar(listOf(listOf(
    APR(ListProductionRule(STMT_LIST, STMT, "\n")),
    TerminalProductionRule(STMT_LIST),
    APR(ListProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")),
    APR(PR(STMT, listOf( // A statement is just a function call going into a variable.
        LowercaseASCIISymbol,
        StringSymbol(":="),
        FUNCTION_NAME,
        StringSymbol("("),
        FUNCTION_ARGS,
        StringSymbol(")"),
    )))),
).flatten(), start = STMT_LIST)



