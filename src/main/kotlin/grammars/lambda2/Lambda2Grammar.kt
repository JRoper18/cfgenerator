package grammars.lambda2

import grammar.*
import grammar.constraints.VariableConstraintGenerator
import grammars.common.*

object Lambda2Grammar {

    const val retTypeAttrName = "retType"

    val maps = StringSymbol("map")
    val mapt = StringSymbol("mapt")
    val filter = StringSymbol("filter")
    val foldl = StringSymbol("foldl")
    val foldr = StringSymbol("foldr")
    val foldt = StringSymbol("foldt")
    val recl = StringSymbol("recl")
    val cons = StringSymbol("cons")
    val lambda = StringSymbol("lambda")

    val lambdaArgs = NtSym("lamargs")
    val stmtSym = NtSym("stmt")
    val programSym = NtSym("prog")
    val basicFunc = NtSym("basicFunc")
    val constant = NtSym("constant")
    val declared = NtSym("declared") // Only used when forcing them to use a var that's already declared.


    val emptyList = StringSymbol("[]")

    val boolConstant = StringsetSymbol(setOf("True", "False"), displayName = "Bool")
    val boolOp = StringsetSymbol(setOf("or", "and", "<", ">", "="), displayName = "BoolOP")
    // It's a boolOp because it RETURNS a bool. It's arguments can be ints.

    val intConstant = StringsetSymbol(intSymbols(-100, 100), displayName = "Int")
    val intOp = StringsetSymbol(setOf("+", "-", "/", "*"), displayName = "IntOP")

    val varName = StringsetSymbol(lowercaseASCII, displayName = "lowercaseAscii")
    val varInit = NtSym("varInit")

    val declaredRule = SynthesizeAttributeProductionRule(mapOf(varName.attributeName to 0), (PR(declared, listOf(varName))))

    val grammar = AttributeGrammar(listOf(
        // Constants
        InitAttributeProductionRule(PR(constant, listOf(intConstant)), retTypeAttrName, "int"),
        InitAttributeProductionRule(PR(constant, listOf(boolConstant)), retTypeAttrName, "bool"),
        InitAttributeProductionRule(PR(constant, listOf(emptyList)), retTypeAttrName, "[int]"),
        // Basic function definitions. We're using prefix notation to make interpretation easier.
        APR(PR(basicFunc, listOf(declared))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 0
            ), PR(basicFunc, listOf(constant))),
        InitAttributeProductionRule(PR(basicFunc, listOf(LP, basicFunc, RP, intOp, LP, basicFunc, RP)), retTypeAttrName, "int"),
        InitAttributeProductionRule(PR(basicFunc, listOf(LP, basicFunc, RP, intOp, LP, basicFunc, RP)), retTypeAttrName, "bool"),
        // Variable declaration
        VariableDeclarationRule(varInit, varName, varName.attributeName),
        // declareds are just variables with the constraint that they're inited/delcared already.
        declaredRule,
        // Lambda initialization.
        SizedListAttributeProductionRule(listName = lambdaArgs, unit = varInit, separator = ","),
        InitAttributeProductionRule(PR(lambdaArgs, listOf(varInit)), "length", "1"),
        // Statement definitions
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 0
            ), PR(stmtSym, listOf(declared))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 0
            ), PR(stmtSym, listOf(constant))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 0
            ), PR(stmtSym, listOf(basicFunc))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 4 // Assuming the second arg is a list already.
            ), PR(stmtSym, listOf(cons, LP, declared, COMMA, declared, RP))),
        HigherOrderSynthesizedRule(
            retTypeAttrName, 2,
            PR(stmtSym, listOf(maps, LP, programSym, COMMA, declared, RP))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 4
            ),
            PR(stmtSym, listOf(filter, LP, programSym, COMMA, declared, RP))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 2
            ), PR(stmtSym, listOf(foldl, LP, programSym, COMMA, constant, COMMA, declared, RP))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 2
            ), PR(stmtSym, listOf(foldr, LP, programSym, COMMA, constant, COMMA, declared, RP))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 2
            ), PR(stmtSym, listOf(recl, LP, programSym, COMMA, constant, COMMA, declared, RP))),
        //Let's ignore trees for now
//        APR(PR(stmtSym, listOf(foldt, LP, programSym, COMMA, constant, COMMA, declared, RP))),
//        APR(PR(stmtSym, listOf(mapt, LP, programSym, COMMA, declared, RP))),
        //Finally:
        SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 3),
            PR(programSym, listOf(lambda, lambdaArgs, COLON, stmtSym))
        ),
    ), start = programSym, constraints = mapOf(
        declaredRule.rule to VariableConstraintGenerator(varName.attributeName)
    ))

}