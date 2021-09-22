package grammars.deepcoder

import grammar.*
import grammars.common.*

private val STMT = NtSym("Stmt")
private val STMT_LIST = NtSym("StmtList")
private val FUNCTION_NAME = StringsetSymbol(listOf(
    "Head",
    "Last",
    "Take",
    "Drop",
))
private val FUNCTION_ARGS = NtSym("FuncArgs")
private val FUNCTION_ARG = NtSym("FuncArg")
val deepCoderGrammar = AttributeGrammar(listOf(listOf(
//    BoundedListProductionRule(STMT_LIST, STMT, "\n", minimumSize = 1),
//    InitIntProductionRule(TerminalProductionRule(STMT_LIST), "length", 0),
    APR(TerminalProductionRule(FUNCTION_ARGS)),
    APR(ListProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")),
    APR(PR(STMT, listOf( // A statement is just a function call going into a variable.
        LowercaseASCIISymbol,
        StringSymbol(":="),
        FUNCTION_NAME,
        StringSymbol("("),
        FUNCTION_ARGS,
        StringSymbol(")"),
    ))),
    APR(ProductionRule(FUNCTION_ARG, listOf(LowercaseASCIISymbol))),
    ),
).flatten(), start = STMT_LIST, constraints = mapOf())



