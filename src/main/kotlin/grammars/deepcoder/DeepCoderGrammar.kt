package grammars.deepcoder

import grammar.*
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.LookupConstraintGenerator
import grammars.common.*

private val STMT = NtSym("Stmt")
private val STMT_LIST = NtSym("StmtList")
private val FUNCTION_NAME = StringsetSymbol(setOf(
    "Head",
    "Last",
    "Take",
    "Drop",
))
private val FUNCTION_ARGS = NtSym("FuncArgs")
private val FUNCTION_ARG = NtSym("FuncArg")
private val STMT_RULE = APR(PR(STMT, listOf( // A statement is just a function call going into a variable.
    LowercaseASCIISymbol,
    StringSymbol(":="),
    FUNCTION_NAME,
    FUNCTION_ARGS,
)))
private val STMT_LIST_RULE = SizedListAttributeProductionRule(STMT_LIST, STMT, "\n")

val deepCoderGrammar = AttributeGrammar(listOf(listOf(
    STMT_LIST_RULE,
    InitAttributeProductionRule(TerminalProductionRule(STMT_LIST), "length", "0"),
    APR(TerminalProductionRule(FUNCTION_ARGS)),
    APR(ListProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")),
    STMT_RULE,
    APR(ProductionRule(FUNCTION_ARG, listOf(LowercaseASCIISymbol))),
    ),
).flatten(), start = STMT_LIST, constraints = mapOf(
    STMT_RULE to LookupConstraintGenerator("chosenSymbol", "length", mapOf(
        "Head" to "2",
        "Last" to "1",
        "Take" to "4",
        "Drop" to "0"
    )),
))



