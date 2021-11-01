package grammars.lambda2

import OrderedSynthesizedAttributeRule
import grammar.*
import grammar.constraints.*
import grammars.common.*

object Lambda2Grammar {

    const val retTypeAttrName = "retType"
    const val intType = "int"
    const val boolType = "bool"
    const val boolListType = "[bool]"
    const val intListType = "[int]"
    const val intListListType = "[[int]]"

    val listTypeMapper = WrapperAttributeMapper()

    val maps = StringSymbol("map")
    val mapt = StringSymbol("mapt")
    val filter = StringSymbol("filter")
    val foldl = StringSymbol("foldl")
    val foldr = StringSymbol("foldr")
    val foldt = StringSymbol("foldt")
    val recl = StringSymbol("recl")
    val cons = StringSymbol("cons")
    val lambda = StringSymbol("lambda")
    val len = StringSymbol("len")
    val minOrMax = StringsetSymbol(setOf("min", "max"))
    val inOrNotIn = StringsetSymbol(setOf("in", "not in"))

    val lambdaArgs = NtSym("lamargs")
    val stmtSym = NtSym("stmt")
    val programSym = NtSym("prog")
    val basicFunc = NtSym("basicFunc")
    val constant = NtSym("constant")
    val declared = NtSym("declared") // Only used when forcing them to use a var that's already declared.


    val emptyList = StringSymbol("[]")

    val boolConstant = StringsetSymbol(setOf("True", "False"), displayName = "Bool")
    val boolToBoolOp = StringsetSymbol(setOf("or", "and", "=="), displayName = "Bool2BoolOp")
    val intToBoolOp = StringsetSymbol(setOf("<", ">", "=="), displayName = "Int2BoolOp")
    // It's a boolOp because it RETURNS a bool. It's arguments can be ints.

    val intConstant = StringsetSymbol(intSymbols(-20, 20), displayName = "Int")
    val intToIntOp = StringsetSymbol(setOf("+", "-", "/", "*"), displayName = "Int2IntOp")

    //Here's a trick: We'll force variables to have names that represent their type.
    val varName = StringsetSymbol(mapOf(
        // Int variables
        "i" to setOf(NodeAttribute(retTypeAttrName, intType)),
        "j" to setOf(NodeAttribute(retTypeAttrName, intType)),
        "k" to setOf(NodeAttribute(retTypeAttrName, intType)),
        "l" to setOf(NodeAttribute(retTypeAttrName, intType)),
        "m" to setOf(NodeAttribute(retTypeAttrName, intType)),
        "n" to setOf(NodeAttribute(retTypeAttrName, intType)),
        // Bool variables
        "o" to setOf(NodeAttribute(retTypeAttrName, boolType)),
        "p" to setOf(NodeAttribute(retTypeAttrName, boolType)),
        "q" to setOf(NodeAttribute(retTypeAttrName, boolType)),
        "r" to setOf(NodeAttribute(retTypeAttrName, boolType)),
        // Int list lists
        "x" to setOf(NodeAttribute(retTypeAttrName, intListListType)),
        "y" to setOf(NodeAttribute(retTypeAttrName, intListListType)),
        "z" to setOf(NodeAttribute(retTypeAttrName, intListListType)),
        "a" to setOf(NodeAttribute(retTypeAttrName, intListListType)),
        "b" to setOf(NodeAttribute(retTypeAttrName, intListListType)),
        "c" to setOf(NodeAttribute(retTypeAttrName, intListListType)),
        // Int lists
        "s" to setOf(NodeAttribute(retTypeAttrName, intListType)),
        "t" to setOf(NodeAttribute(retTypeAttrName, intListType)),
        "u" to setOf(NodeAttribute(retTypeAttrName, intListType)),
        "v" to setOf(NodeAttribute(retTypeAttrName, intListType)),
        "w" to setOf(NodeAttribute(retTypeAttrName, intListType)),
        ), displayName = "lowercaseAscii")
    val varInit = NtSym("varInit")

    val newVarRule = VariableDeclarationRule(varInit, varName, varName.attributeName)
    val typedNewVarRule = VariableChildValueRule(varInit, varName, newVarRule.subruleVarnameAttributeKey, "_type", retTypeAttrName)
    val declaredRule = SynthesizeAttributeProductionRule(mapOf(varName.attributeName to 0, retTypeAttrName to 0), (PR(declared, listOf(varName))))

    val lambdaArgsRule = SizedListAttributeProductionRule(listName = lambdaArgs, unit = varInit, separator = ",")
    val lambdaArgsInitRule = InitAttributeProductionRule(PR(lambdaArgs, listOf(varInit)), "length", "1")

    val filterRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 4
        ),
        PR(stmtSym, listOf(filter, LP, programSym, COMMA, declared, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val mapRule = AttributeMappingProductionRule(
        PR(stmtSym, listOf(maps, LP, programSym, COMMA, declared, RP)), retTypeAttrName, 2, listTypeMapper)

    val foldlRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 2
        ), PR(stmtSym, listOf(foldl, LP, programSym, COMMA, constant, COMMA, declared, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val foldrRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 2
        ), PR(stmtSym, listOf(foldr, LP, programSym, COMMA, constant, COMMA, declared, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val reclRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 2
        ), PR(stmtSym, listOf(recl, LP, programSym, COMMA, constant, COMMA, declared, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val consRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 4 // Assuming the second arg is a list already.
        ), PR(stmtSym, listOf(cons, LP, stmtSym, COMMA, stmtSym, RP)))

    val minOrMaxRule = InitAttributeProductionRule(PR(stmtSym, listOf(minOrMax, LP, stmtSym, RP)), retTypeAttrName, intType).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val indexIntoRule = AttributeMappingProductionRule(PR(stmtSym, listOf(stmtSym, LSB, stmtSym, RSB)), retTypeAttrName, 0, listTypeMapper.inverse()).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }


    val indexIntoRangeRule = SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 0), PR(stmtSym, listOf(stmtSym, LSB, stmtSym, COLON, stmtSym, RSB))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2), Pair(retTypeAttrName, 4)), it)
    }

    val listContainsRule = InitAttributeProductionRule(PR(stmtSym, listOf(stmtSym, inOrNotIn, stmtSym)), retTypeAttrName, boolType)

    val int2BoolRule = InitAttributeProductionRule(PR(basicFunc, listOf(LP, basicFunc, RP, intToBoolOp, LP, basicFunc, RP)), retTypeAttrName, "bool")
    val int2IntRule = InitAttributeProductionRule(PR(basicFunc, listOf(LP, basicFunc, RP, intToIntOp, LP, basicFunc, RP)), retTypeAttrName, "int")
    val bool2BoolRule = InitAttributeProductionRule(PR(basicFunc, listOf(LP, basicFunc, RP, boolToBoolOp, LP, basicFunc, RP)), retTypeAttrName, "bool")


    val grammar = AttributeGrammar(listOf(
        // Constants
        InitAttributeProductionRule(PR(constant, listOf(intConstant)), retTypeAttrName, "int"),
        InitAttributeProductionRule(PR(constant, listOf(boolConstant)), retTypeAttrName, "bool"),

        // Let's just say empty lists are int lists, for now.
        InitAttributeProductionRule(PR(constant, listOf(emptyList)), retTypeAttrName, "[int]"),
        // Basic function definitions.
//        APR(PR(basicFunc, listOf(declared))),
//        SynthesizeAttributeProductionRule(
//            mapOf(
//                retTypeAttrName to 0
//            ), PR(basicFunc, listOf(constant))),
//        int2BoolRule,
//        int2IntRule,
//        bool2BoolRule,

        // Variable declaration
        newVarRule.withOtherRule(typedNewVarRule),
        // declareds are just variables with the constraint that they're inited/delcared already.
        declaredRule,
        // Lambda initialization.
        lambdaArgsRule,
        lambdaArgsInitRule,
        // Statement definitions
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 0
            ), PR(stmtSym, listOf(declared))),
        SynthesizeAttributeProductionRule(
            mapOf(
                retTypeAttrName to 0
            ), PR(stmtSym, listOf(constant))),
//        SynthesizeAttributeProductionRule(
//            mapOf(
//                retTypeAttrName to 0
//            ), PR(stmtSym, listOf(basicFunc))),

        consRule,
        // Len
        InitAttributeProductionRule(PR(stmtSym, listOf(len, LP, declared, RP)), retTypeAttrName, intType),
        // min and max
        minOrMaxRule,
        // Indexers
        indexIntoRule,
        indexIntoRangeRule,
        listContainsRule,
        // Higher-order stuff.
        mapRule,
        filterRule,
        foldlRule,
        foldrRule,
        reclRule,

        //Let's ignore trees for now
//        APR(PR(stmtSym, listOf(foldt, LP, programSym, COMMA, constant, COMMA, declared, RP))),
//        APR(PR(stmtSym, listOf(mapt, LP, programSym, COMMA, declared, RP))),
        //Finally:
        SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 3),
            PR(programSym, listOf(lambda, lambdaArgs, COLON, stmtSym))
        ),
    ), start = programSym, constraints = mapOf(
        // Make sure variables are declared before we use them.
        declaredRule.rule to VariableConstraintGenerator(varName.attributeName, newVarRule),

        // Filters need functions that return bools
        filterRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", boolType)))),

        // Folds and recls should have 2nd children (input functions) that return the same stuff they function returns.
        foldlRule.rule to EqualAttributeValueConstraintGenerator(setOf("2.${retTypeAttrName}", retTypeAttrName)),
        foldrRule.rule to EqualAttributeValueConstraintGenerator(setOf("2.${retTypeAttrName}", retTypeAttrName)),
        reclRule.rule to EqualAttributeValueConstraintGenerator(setOf("2.${retTypeAttrName}", retTypeAttrName)),

        // Cons needs a list as a return value.
        consRule.rule to BasicConstraintGenerator(listOf(OrRuleConstraint(listOf(boolListType, intListListType, intListType).map{
            BasicRuleConstraint(NodeAttribute(retTypeAttrName, it))
        }))),

        // min/max needs int list
        minOrMaxRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", intListType)))),

        // Indexers need integer indicies
        indexIntoRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", intType)))),
        indexIntoRangeRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", intType)),
            BasicRuleConstraint(NodeAttribute("4.${retTypeAttrName}", intType)))),


    ))


}