package grammars.lambda2

import grammar.*
import grammar.constraints.VariableConstraintGenerator
import grammars.common.*
import grammars.deepcoder.DeepCoderGrammar

object Lambda2Grammar {
    val maps = StringSymbol("map")
    val mapt = StringSymbol("mapt")
    val filter = StringSymbol("filter")
    val foldl = StringSymbol("foldl")
    val foldr = StringSymbol("foldr")
    val foldt = StringSymbol("foldt")
    val recl = StringSymbol("recl")
    val cons = StringSymbol("cons")
    val lambda = StringSymbol("lambda")
    val returnS = StringSymbol("return")

    val lambdaArgs = NtSym("lamargs")
    val stmtSym = NtSym("stmt")
    val programSym = NtSym("prog")
    val basicFunc = NtSym("basicFunc")
    val constant = NtSym("constant")
    val usedVar = NtSym("usedVar") // Only used when forcing them to use a var that's already declared.

    val boolConstant = StringsetSymbol(setOf("True", "False"), displayName = "Bool")
    val boolOp = StringsetSymbol(setOf("or", "and"), displayName = "BoolOP")

    val intConstant = StringsetSymbol(intSymbols(-100, 100), displayName = "Int")
    val intOp = StringsetSymbol(setOf("+", "-", "/", "*", "<", ">", "="), displayName = "IntOP")

    val varName = StringsetSymbol(lowercaseASCII, displayName = "lowercaseAscii")
    val varInit = NtSym("varInit")

    val usedVarRule = SynthesizeAttributeProductionRule(mapOf(varName.attributeName to 0), (PR(usedVar, listOf(varName))))

    val grammar = AttributeGrammar(listOf(
        // Constants
        APR(PR(constant, listOf(intConstant))), // For now say it's just ints.
        // Basic function definitions. We're using prefix notation to make interpretation easier.
        APR(PR(basicFunc, listOf(usedVar))),
        APR(PR(basicFunc, listOf(intConstant))),
        APR(PR(basicFunc, listOf(boolConstant))),
        APR(PR(basicFunc, listOf(LP, basicFunc, RP, intOp, LP, basicFunc, RP))),
        APR(PR(basicFunc, listOf(LP, basicFunc, RP, boolOp, LP, basicFunc, RP))),
        // Variable declaration
        VariableDeclarationRule(varInit, varName, varName.attributeName),
        // UsedVars are just variables with the constraint that they're inited/delcared already.
        usedVarRule,
        // Lambda initialization.
        SizedListAttributeProductionRule(listName = lambdaArgs, unit = varInit, separator = ","),
        InitAttributeProductionRule(PR(lambdaArgs, listOf(varInit)), "length", "1"),
        // Statement definitions
        APR(PR(stmtSym, listOf(usedVar))),
        APR(PR(stmtSym, listOf(boolConstant))),
        APR(PR(stmtSym, listOf(basicFunc))),
        APR(PR(stmtSym, listOf(cons, LP, usedVar, COMMA, usedVar, RP))),
        APR(PR(stmtSym, listOf(maps, LP, programSym, COMMA, usedVar, RP))),
        APR(PR(stmtSym, listOf(filter, LP, programSym, COMMA, usedVar, RP))),
        APR(PR(stmtSym, listOf(foldl, LP, programSym, COMMA, constant, COMMA, usedVar, RP))),
        APR(PR(stmtSym, listOf(foldr, LP, programSym, COMMA, constant, COMMA, usedVar, RP))),
        APR(PR(stmtSym, listOf(recl, LP, programSym, COMMA, constant, COMMA, usedVar, RP))),
        //Let's ignore trees for now
//        APR(PR(stmtSym, listOf(foldt, LP, programSym, COMMA, constant, COMMA, usedVar, RP))),
//        APR(PR(stmtSym, listOf(mapt, LP, programSym, COMMA, usedVar, RP))),
        //Finally:
        APR(PR(programSym, listOf(lambda, lambdaArgs, COLON, stmtSym)))
    ), start = programSym, constraints = mapOf(
        usedVarRule.rule to VariableConstraintGenerator(varName.attributeName)
    ))

}