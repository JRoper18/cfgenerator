package grammars.deepcoder

import grammar.*
import grammar.constraints.LookupConstraintGenerator
import grammar.constraints.VariableConstraintGenerator
import grammars.common.rules.*

object DeepCoderGrammar {
    val STMT = NtSym("Stmt")
    val STMT_LIST = NtSym("StmtList")
    val VARNAME = NtSym("VarName")
    const val functionNameAttr = "functionName"
    const val typeNameAttr = "typeName"
    const val listType = "[int]"
    const val intType = "int"
    const val varAttrName = "varName"
    const val lambdaAttrName = "chosenLambda"
    val LAMBDA_FUNC = StringsetSymbol(setOf(
        "(+1)", "(-1)", "(*2)", "(/2)", "(*(-1))", "(**2)", "(*3)", "(/3)", "(*4)", "(/4)", //Int -> int
        "(>0)", "(<0)", "(%2==0)", "(%2 == 1)", //Int -> bool
        "(+)", "(-)", "(*)", "MIN", "MAX" // Int -> int -> int
    ), displayName = "Lambda", attributeName = lambdaAttrName)

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
        // And the higher-order functions
        "Map" to setOf(NodeAttribute(typeNameAttr, listType)),
        "Filter" to setOf(NodeAttribute(typeNameAttr, listType)),
        "Count" to setOf(NodeAttribute(typeNameAttr, intType)),
        "ZipWith" to setOf(NodeAttribute(typeNameAttr, listType)),
        "ScanL1" to setOf(NodeAttribute(typeNameAttr, listType)),
    ), attributeName = functionNameAttr, displayName = "FUNCTION_NAME")
    val FUNCTION_ARGS = NtSym("FuncArgs")
    val FUNCTION_ARG = NtSym("FuncArg")
    val VARDEF = NtSym("VarDef")
    val STMT_RULE = SynthesizeAttributeProductionRule(mapOf(varAttrName to 0),
        PR(STMT, listOf( // A statement is just a function call going into a variable.
            VARNAME,
            StringSymbol(":="),
            VARDEF,
        )))
    var VARNAME_STRINGSET = StringsetSymbol(lowercaseASCII, displayName = "lowercaseASCII", attributeName = varAttrName)
    val VARDECL_RULE_BASE = VariableDeclarationRule(VARNAME, VARNAME_STRINGSET, varAttrName)
    val VARDECL_RULE = SynthesizeAttributeProductionRule(mapOf(varAttrName to 0), VARDECL_RULE_BASE.rule).withOtherRule(VARDECL_RULE_BASE)
    val TYPES = StringsetSymbol(setOf("[int]", "int"), attributeName = typeNameAttr)
    val FUNCTION_CALL_RULE = SynthesizeAttributeProductionRule(mapOf(functionNameAttr to 0, "length" to 2, typeNameAttr to 0),
        PR(VARDEF, listOf(FUNCTION_NAME, StringSymbol(" "), FUNCTION_ARGS)))
    val TYPEVAR_RULE = SynthesizeAttributeProductionRule(mapOf(typeNameAttr to 0),
        PR(VARDEF, listOf(TYPES)))
    val STMT_LIST_RULE = SizedListAttributeProductionRule(STMT_LIST, STMT, "\n")
    val FUNCTION_LIST_RULE = SizedListAttributeProductionRule(FUNCTION_ARGS, FUNCTION_ARG, " ")
    val LIST_INIT_RULE = InitAttributeProductionRule(ProductionRule(STMT_LIST, listOf(STMT)), "length", "1")
    val INIT_FUNCTION_ARGS_RULE = InitAttributeProductionRule(ProductionRule(FUNCTION_ARGS, listOf(FUNCTION_ARG)), "length", "1")
    val FUNCARG_VARIABLE = SynthesizeAttributeProductionRule(mapOf(varAttrName to 0), ProductionRule(FUNCTION_ARG, listOf(VARNAME_STRINGSET)))
    val FUNCARG_LAMBDA = SynthesizeAttributeProductionRule(mapOf(lambdaAttrName to 0), ProductionRule(FUNCTION_ARG, listOf(LAMBDA_FUNC))) // A lambda symbol

    val grammar = AttributeGrammar(listOf(
        STMT_LIST_RULE,
        VARDECL_RULE,
        LIST_INIT_RULE,
        INIT_FUNCTION_ARGS_RULE,
        FUNCTION_LIST_RULE,
        FUNCTION_CALL_RULE,
        STMT_RULE,
        TYPEVAR_RULE,
        FUNCARG_VARIABLE, // A varname, or...
        FUNCARG_LAMBDA,
    ), start = STMT_LIST, constraints = mapOf(
        FUNCTION_CALL_RULE.rule to LookupConstraintGenerator(functionNameAttr, "length", mapOf(
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
            "Sum" to "1",
            "Map" to "2",
            "Filter" to "2",
            "Count" to "2",
            "ZipWith" to "3",
            "ScanL1" to "2",
        )),
        FUNCARG_VARIABLE.rule to VariableConstraintGenerator(varAttrName, VARDECL_RULE_BASE)
    ))
}






