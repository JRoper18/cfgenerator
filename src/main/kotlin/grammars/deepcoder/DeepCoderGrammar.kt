package grammars.deepcoder

import grammar.*
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.LookupConstraintGenerator
import grammars.common.*

private val STMT = NtSym("Stmt")
private val STMT_LIST = NtSym("StmtList")
const val functionNameAttr = "functionName"
const val typeNameAttr = "typeName"
val FUNCTION_NAME = StringsetSymbol(setOf(
    "Head",
    "Last",
    "Take",
    "Drop",
), attributeName = functionNameAttr)
private val FUNCTION_ARGS = NtSym("FuncArgs")
private val FUNCTION_ARG = NtSym("FuncArg")
private val VARDEF = NtSym("VarDef")
private val STMT_RULE = SynthesizeAttributeProductionRule(mapOf(functionNameAttr to 2, "length" to 2),
    PR(STMT, listOf( // A statement is just a function call going into a variable.
    LowercaseASCIISymbol,
    StringSymbol(":="),
    VARDEF,
)))
private val TYPES = StringsetSymbol(setOf("[int]", "int"))
private val FUNCTION_CALL_RULE = SynthesizeAttributeProductionRule(mapOf(functionNameAttr to 0, "length" to 1),
    PR(VARDEF, listOf(FUNCTION_NAME, FUNCTION_ARGS)))
private val INPUT_VAR_RULE = APR(PR(VARDEF, listOf(TYPES)))
private val STMT_LIST_RULE = SizedListAttributeProductionRule(STMT_LIST, STMT, "\n")
private val FUNCTION_LIST_RULE = SizedListAttributeProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")
val deepCoderGrammar = AttributeGrammar(listOf(
    STMT_LIST_RULE,
    InitAttributeProductionRule(TerminalProductionRule(STMT_LIST), "length", "0"),
    InitAttributeProductionRule(TerminalProductionRule(FUNCTION_ARGS), "length", "0"),
    FUNCTION_LIST_RULE,
    FUNCTION_CALL_RULE,
    STMT_RULE,
    APR(ProductionRule(FUNCTION_ARG, listOf(LowercaseASCIISymbol))),
), start = STMT_LIST, constraints = mapOf(
    FUNCTION_CALL_RULE to LookupConstraintGenerator(functionNameAttr, "length", mapOf(
        // Gets the length of args = length of function call
        "Head" to "2",
        "Last" to "1",
        "Take" to "4",
        "Drop" to "3"
    )),
))



