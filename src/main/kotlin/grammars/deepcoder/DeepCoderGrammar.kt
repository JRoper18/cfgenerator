package grammars.deepcoder

import grammar.*
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.LookupConstraintGenerator
import grammars.common.*

private val STMT = NtSym("Stmt")
private val STMT_LIST = NtSym("StmtList")
val FUNCTION_NAME = StringsetSymbol(setOf(
    "Head",
    "Last",
    "Take",
    "Drop",
))
private val FUNCTION_ARGS = NtSym("FuncArgs")
private val FUNCTION_ARG = NtSym("FuncArg")
private val STMT_RULE = SynthesizeAttributeProductionRule(mapOf("chosenSymbol" to 2, "length" to 3),
    PR(STMT, listOf( // A statement is just a function call going into a variable.
    LowercaseASCIISymbol,
    StringSymbol(":="),
    FUNCTION_NAME,
    FUNCTION_ARGS,
)))
private val STMT_LIST_RULE = SizedListAttributeProductionRule(STMT_LIST, STMT, "\n")
private val FUNCTION_LIST_RULE = SizedListAttributeProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")
val deepCoderGrammar = AttributeGrammar(listOf(
    STMT_LIST_RULE,
    InitAttributeProductionRule(TerminalProductionRule(STMT_LIST), "length", "0"),
    InitAttributeProductionRule(TerminalProductionRule(FUNCTION_ARGS), "length", "0"),
    FUNCTION_LIST_RULE,
    STMT_RULE,
    APR(ProductionRule(FUNCTION_ARG, listOf(LowercaseASCIISymbol))),
), start = STMT_LIST, constraints = mapOf(
    STMT_RULE to LookupConstraintGenerator("chosenSymbol", "length", mapOf(
        "Head" to "2",
        "Last" to "1",
        "Take" to "4",
        "Drop" to "3"
    )),
))



