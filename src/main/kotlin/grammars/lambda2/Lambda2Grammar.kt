package grammars.lambda2

import grammar.*
import grammars.common.*

object Lambda2Grammar {
    val maps = StringSymbol("map")
    val mapt = StringSymbol("mapt")
    val filter = StringSymbol("filter")
    val foldl = StringSymbol("foldl")
    val foldr = StringSymbol("foldr")
    val foldt = StringSymbol("foldt")
    val recl = StringSymbol("recl")
    val cons = StringSymbol("cons")
    val funcSym = NtSym("func")
    val stmtSym = NtSym("stmt")
    val programSym = NtSym("prog")
    val basicFunc = NtSym("basicFunc")
    val constant = NtSym("constant")

    val boolConstant = StringsetSymbol(setOf("True", "False"), displayName = "Bool")
    val boolOp = StringsetSymbol(setOf("||", "&&"), displayName = "BoolOP")

    val intConstant = StringsetSymbol(intSymbols(-100, 100), displayName = "Int")
    val intOp = StringsetSymbol(setOf("+", "-", "/", "*", "<", ">", "="), displayName = "IntOP")

    val varName = StringsetSymbol(lowercaseASCII + setOf("_"), displayName = "lowercaseAscii") // "_" is input var
    val varInit = NtSym("varInit")

    val grammar = AttributeGrammar(listOf(
        // Constants
        APR(PR(constant, listOf(intConstant))), // For now say it's just ints.
        // Basic function definitions
        APR(PR(basicFunc, listOf(LP, varName, intOp, varName, RP))),
        APR(PR(basicFunc, listOf(LP, varName, boolOp, varName, RP))),
        APR(PR(basicFunc, listOf(LP, intConstant, intOp, varName, RP))),
        APR(PR(basicFunc, listOf(LP, varName, intOp, intConstant, RP))),
        APR(PR(basicFunc, listOf(LP, boolConstant, boolOp, varName, RP))),
        APR(PR(basicFunc, listOf(LP, varName, boolOp, boolConstant, RP))),
        // Variable declaration
        VariableDeclarationRule(varInit, varName, varName.attributeName),
        // Statement definitions
        APR(PR(stmtSym, listOf(varInit))),
        APR(PR(stmtSym, listOf(boolConstant))),
        APR(PR(stmtSym, listOf(programSym, programSym))),
        APR(PR(stmtSym, listOf(basicFunc))),
        APR(PR(stmtSym, listOf(cons, LP, varName, varName, RP))),
        APR(PR(stmtSym, listOf(maps, programSym, varName))),
        APR(PR(stmtSym, listOf(mapt, programSym, varName))),
        APR(PR(stmtSym, listOf(filter, programSym, varName))),
        APR(PR(stmtSym, listOf(foldl, programSym, constant, varName))),
        APR(PR(stmtSym, listOf(foldr, programSym, constant, varName))),
        APR(PR(stmtSym, listOf(foldt, programSym, constant, varName))),
        APR(PR(stmtSym, listOf(recl, programSym, constant, varName))),
        //Finally:
        APR(PR(programSym, listOf(LP, stmtSym, RP)))
    ), start = programSym, constraints = mapOf())

}