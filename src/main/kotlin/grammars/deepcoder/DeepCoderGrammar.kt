package grammars.deepcoder

import grammar.*
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.LookupConstraintGenerator
import grammars.common.*

internal val STMT = NtSym("Stmt")
internal val STMT_LIST = NtSym("StmtList")
internal val LAMBDA_FUNC = StringsetSymbol(setOf(
    "(+1)", "(-1)", "(*2)", "(/2)", "(*(-1))", "(**2)", "(*3)", "(/3)", "(*4)", "(/4)", //Int -> int
    "(>0)", "(<0)", "(%2 == 0)", "(%2 == 1)", //Int -> bool
    "(+)", "(-)", "(*)", "MIN", "MAX" // Int -> int -> int
), displayName = "Lambda")
const val functionNameAttr = "functionName"
const val typeNameAttr = "typeName"
const val listType = "[int]"
const val intType = "int"
val FUNCTION_NAME = StringsetSymbol(mapOf(
    "Head" to setOf(NodeAttribute(typeNameAttr, intType)),
    "Last" to setOf(NodeAttribute(typeNameAttr, intType)),
    "Take" to setOf(NodeAttribute(typeNameAttr, listType)),
    "Drop" to setOf(NodeAttribute(typeNameAttr, listType)),
    "Access" to setOf(NodeAttribute(typeNameAttr, intType)),
    "Minimum" to setOf(NodeAttribute(typeNameAttr, intType)),
    "Maximum" to setOf(NodeAttribute(typeNameAttr, intType)),
    "Reverse" to setOf(NodeAttribute(typeNameAttr, listType)),
    "Sort" to setOf(NodeAttribute(typeNameAttr, listType)),
    "Sum" to setOf(NodeAttribute(typeNameAttr, intType)),
    ), attributeName = functionNameAttr)
internal val FUNCTION_ARGS = NtSym("FuncArgs")
internal val FUNCTION_ARG = NtSym("FuncArg")
internal val VARDEF = NtSym("VarDef")
const val varAttrName = "varName"
internal val STMT_RULE = SynthesizeAttributeProductionRule(mapOf(varAttrName to 0),
    PR(STMT, listOf( // A statement is just a function call going into a variable.
    StringsetSymbol(lowercaseASCII, displayName = "lowercaseASCII", attributeName = varAttrName),
    StringSymbol(":="),
    VARDEF,
)))
internal val TYPES = StringsetSymbol(setOf("[int]", "int"), attributeName = typeNameAttr)
internal val FUNCTION_CALL_RULE = SynthesizeAttributeProductionRule(mapOf(functionNameAttr to 0, "length" to 1, typeNameAttr to 0),
    PR(VARDEF, listOf(FUNCTION_NAME, FUNCTION_ARGS)))
internal val TYPEVAR_RULE = SynthesizeAttributeProductionRule(mapOf(typeNameAttr to 0),
    PR(VARDEF, listOf(TYPES)))
internal val STMT_LIST_RULE = SizedListAttributeProductionRule(STMT_LIST, STMT, "\n")
internal val FUNCTION_LIST_RULE = SizedListAttributeProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")
val deepCoderGrammar = AttributeGrammar(listOf(
    STMT_LIST_RULE,
    InitAttributeProductionRule(TerminalProductionRule(STMT_LIST), "length", "0"),
    InitAttributeProductionRule(TerminalProductionRule(FUNCTION_ARGS), "length", "0"),
    FUNCTION_LIST_RULE,
    FUNCTION_CALL_RULE,
    STMT_RULE,
    TYPEVAR_RULE,
    APR(ProductionRule(FUNCTION_ARG, listOf(LowercaseASCIISymbol))), // A varname, or...
//    APR(ProductionRule(FUNCTION_ARG, listOf(LAMBDA_FUNC))), // A lambda symbol
), start = STMT_LIST, constraints = mapOf(
    FUNCTION_CALL_RULE to LookupConstraintGenerator(functionNameAttr, "length", mapOf(
        // Gets the length of args = length of function call
        "Head" to "1",
        "Last" to "1",
        "Take" to "2",
        "Drop" to "2",
        "Access" to "2",
        "Minimum" to "1",
        "Maximum" to "1",
        "Reverse" to "1",
        "Sort" to "1",
        "Sum" to "1"
    )),
))





