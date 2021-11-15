package grammars.lambda2

import grammars.common.rules.OrderedSynthesizedAttributeRule
import grammar.*
import grammar.constraints.*
import grammars.common.mappers.WrapperAttributeMapper
import grammars.common.rules.*

object Lambda2Grammar {

    const val retTypeAttrName = "retType"
    const val intType = "int"
    const val boolType = "bool"
    const val boolListType = "[bool]"
    const val intListType = "[int]"
    const val intListListType = "[[int]]"
    val listTypes = setOf(boolListType, intListType, intListListType)
    val basicTypes = setOf(intType, boolType, boolListType, intListType, intListListType)
    val listTypeMapper = WrapperAttributeMapper()

    val maps = StringSymbol("map")
    val mapt = StringSymbol("mapt")
    val filter = StringSymbol("filter")
    val foldAny = StringsetSymbol(setOf("foldl", "foldr"))
    val foldt = StringSymbol("foldt")
    val recl = StringSymbol("recl")
    val cons = StringSymbol("cons")
    val concat = StringSymbol("concat")
    val lambda = StringSymbol("lambda")
    val len = StringSymbol("len")
    val minOrMax = StringsetSymbol(setOf("min", "max"))
    val inOrNotIn = StringsetSymbol(setOf("in", "not in"))

    val lambdaArgs = NtSym("lamargs")
    val stmtSym = NtSym("stmt")
    val programSym = NtSym("prog")
    val declared = NtSym("declared") // Only used when forcing them to use a var that's already declared.


    val emptyList = StringSymbol("[]")

    val boolConstant = StringsetSymbol(setOf("True", "False"), displayName = "Bool")
    val boolToBoolOp = StringsetSymbol(setOf("or", "and", "=="), displayName = "Bool2BoolOp")
    val intToBoolOp = StringsetSymbol(setOf("<", ">", "=="), displayName = "Int2BoolOp")

    // Keep the int constant range low to help GPT. It doesn't like to have too many options.  
    val intConstant = StringsetSymbol(intSymbols(-1, 5), displayName = "Int")
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
    val typedNewVarRule = VariableChildValueRule(varInit, varName, newVarRule.subruleVarnameAttributeKey, "_type", retTypeAttrName).withOtherRule {
        SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 0), it) // Bring up the ret type
    }
    val declaredRule = SynthesizeAttributeProductionRule(mapOf(varName.attributeName to 0, retTypeAttrName to 0), (PR(declared, listOf(varName))))
    val lambdaArgsRule = OrderedListAttributeRule(ListProductionRule(listName = lambdaArgs, unitName = varInit, separator = ","), retTypeAttrName, 3)
    val lambdaArgsInitRule = lambdaArgsRule.initListRule(0, PR(lambdaArgs, listOf(varInit)))


    val filterRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 4,
            "length" to 2,
        ),
        PR(stmtSym, listOf(filter, LP, programSym, COMMA, stmtSym, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val mapRule = AttributeMappingProductionRule(
        PR(stmtSym, listOf(maps, LP, programSym, COMMA, stmtSym, RP)), retTypeAttrName, 2, listTypeMapper).withOtherRule {
            OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 4)), it)
    }

    val foldAnyRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 2,
            "length" to 2, // Need the # of args for constraint purposes.
            "0.${retTypeAttrName}" to 2, // We also want the types of the first arg of the lambda... also for constraints.
        ), PR(stmtSym, listOf(foldAny, LP, programSym, COMMA, stmtSym, COMMA, stmtSym, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 4), Pair(retTypeAttrName, 6)), it).withOtherRule {
            // Finally, we also need the list-ified type of the 2nd arg of the lambda, to make sure it matches with the type of the 2nd total arg.
            AttributeMappingProductionRule(it, "1.${retTypeAttrName}", 2, listTypeMapper)
        }}

    val reclRule = SynthesizeAttributeProductionRule(
        mapOf(
            retTypeAttrName to 2,
            "length" to 2, // Need the # of args for constraint purposes.
            "1.${retTypeAttrName}" to 2
        ), PR(stmtSym, listOf(recl, LP, programSym, COMMA, stmtSym, COMMA, stmtSym, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2), Pair(retTypeAttrName, 4), Pair(retTypeAttrName, 6)), it).withOtherRule {
            AttributeMappingProductionRule(it, "0.${retTypeAttrName}", 2, listTypeMapper)
        }
    }
    // This gives us the list-ified 0th arg to the lambda, the 1st arg to the lambda, the length of the lambda's args,
    // And the return type of the lambda, the type of the constant, and the type of the variable.

    val consRule = AttributeMappingProductionRule(
        PR(stmtSym, listOf(cons, LP, stmtSym, COMMA, stmtSym, RP)),
        // Makes a list of the 2nd child's type
        retTypeAttrName, 2, listTypeMapper).withOtherRule {
        // Bring up the list that's being cons'd to. We'll check that it and the retType are the same.
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 4)), it)
    }

    val minOrMaxRule = InitAttributeProductionRule(PR(stmtSym, listOf(minOrMax, LP, stmtSym, RP)), retTypeAttrName, intType).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it)
    }

    val indexIntoRule = AttributeMappingProductionRule(PR(stmtSym, listOf(stmtSym, LSB, stmtSym, RSB)), retTypeAttrName, 0, listTypeMapper.inverse()).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2), Pair(retTypeAttrName, 0)), it)
    }


    val indexIntoRangeRule = SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 0), PR(stmtSym, listOf(stmtSym, LSB, stmtSym, COLON, stmtSym, RSB))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 0), Pair(retTypeAttrName, 2), Pair(retTypeAttrName, 4)), it)
    }

    val listContainsRule = InitAttributeProductionRule(PR(stmtSym, listOf(stmtSym, inOrNotIn, stmtSym)), retTypeAttrName, boolType).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 0), Pair(retTypeAttrName, 2)), it)
    }

    val lenRule = InitAttributeProductionRule(PR(stmtSym, listOf(len, LP, stmtSym, RP)), retTypeAttrName, intType).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 2)), it) // So we can make a constraint that len() only takes lists.
    }

    val concatRule = SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 2), PR(stmtSym, listOf(concat, LP, stmtSym, COMMA, stmtSym, RP))).withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 4)), it)
    }


    val int2BoolRule = InitAttributeProductionRule(PR(stmtSym, listOf(LP, stmtSym, RP, intToBoolOp, LP, stmtSym, RP)), retTypeAttrName, "bool").withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 1), Pair(retTypeAttrName, 5)), it)
    }
    val int2IntRule = InitAttributeProductionRule(PR(stmtSym, listOf(LP, stmtSym, RP, intToIntOp, LP, stmtSym, RP)), retTypeAttrName, "int").withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 1), Pair(retTypeAttrName, 5)), it)
    }
    val bool2BoolRule = InitAttributeProductionRule(PR(stmtSym, listOf(LP, stmtSym, RP, boolToBoolOp, LP, stmtSym, RP)), retTypeAttrName, "bool").withOtherRule {
        OrderedSynthesizedAttributeRule(setOf(Pair(retTypeAttrName, 1), Pair(retTypeAttrName, 5)), it)
    }

    fun isListConstraint(attrKey : String) : RuleConstraint {
        return OrRuleConstraint(listOf(boolListType, intListListType, intListType).map{
            BasicRuleConstraint(NodeAttribute(attrKey, it))
        })
    }

    val totalRootLambdaRule = SynthesizeAttributeProductionRule(mapOf(retTypeAttrName to 3) + lambdaArgsRule.attrKeysMade.map {
        Pair(it, 1) // Inherit every attribute from the lambdaArgs
    }.toMap(),
        PR(programSym, listOf(lambda, lambdaArgs, COLON, stmtSym)),
    )


    val grammar = AttributeGrammar(listOf(
        // Constants
        InitAttributeProductionRule(PR(stmtSym, listOf(intConstant)), retTypeAttrName, "int"),
        InitAttributeProductionRule(PR(stmtSym, listOf(boolConstant)), retTypeAttrName, "bool"),

        // Let's just say empty lists are int lists, for now.
        InitAttributeProductionRule(PR(stmtSym, listOf(emptyList)), retTypeAttrName, "[int]"),

        // Basic function definitions.
        int2BoolRule,
        int2IntRule,
        bool2BoolRule,

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
        consRule,
        concatRule,
        // Len
        lenRule,
        // min and max
        minOrMaxRule,
        // Indexers
        indexIntoRule,
        indexIntoRangeRule,
        listContainsRule,

        // Higher-order stuff.
        mapRule,
        filterRule,
        foldAnyRule,
        reclRule,

        //Let's ignore trees for now
//        APR(PR(stmtSym, listOf(foldt, LP, programSym, COMMA, constant, COMMA, declared, RP))),
//        APR(PR(stmtSym, listOf(mapt, LP, programSym, COMMA, declared, RP))),
        //Finally:
        totalRootLambdaRule,
    ), start = programSym, constraints = mapOf(
        // Make sure variables are declared before we use them.
        declaredRule.rule to VariableConstraintGenerator(varName.attributeName, newVarRule),

        // Filters need functions that return bools
        filterRule.rule to BasicConstraintGenerator(listOf(
            BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", boolType)),
            // And the length of their lambda args needs to be 1
//             BasicRuleConstraint(NodeAttribute("length", "1")),
        )),

        // Maps need a list as input.
        mapRule.rule to BasicConstraintGenerator(listOf(isListConstraint("4.${retTypeAttrName}"))),

        // Folds and recls should have 2nd children (input functions) that return the same stuff they function returns,
        // and that the return type of the 2nd arg of fold is the same
        // They also need the first arg of their child lambda to be therae same as the return type

        foldAnyRule.rule to EqualAttributeValueConstraintGenerator(setOf(retTypeAttrName,
            "4.${retTypeAttrName}", "0.${retTypeAttrName}"), possibleValues = basicTypes).and(
            //AND that the list-ified 1st arg of the lambda matches the 2nd arg of fold
            EqualAttributeValueConstraintGenerator(setOf("1.${retTypeAttrName}", "6.${retTypeAttrName}"), possibleValues = listTypes)
        ).and(
            // and the lambda needs 2 args
            BasicConstraintGenerator(BasicRuleConstraint(NodeAttribute("length", "2")))
        ),

        reclRule.rule to EqualAttributeValueConstraintGenerator(setOf(retTypeAttrName, "4.${retTypeAttrName}"), possibleValues = basicTypes).and(
            EqualAttributeValueConstraintGenerator(setOf("1.${retTypeAttrName}", "6.${retTypeAttrName}", "0.${retTypeAttrName}"), possibleValues = listTypes)
        ).and(
            BasicConstraintGenerator(BasicRuleConstraint(NodeAttribute("length", "2")))
        ),

        // Cons needs a list as a return value and a list as an input value.
        consRule.rule to BasicConstraintGenerator(listOf(isListConstraint(retTypeAttrName))).and(
            // Check that the list returned is the same type as the list arg of cons.
            EqualAttributeValueConstraintGenerator(setOf("4.${retTypeAttrName}", retTypeAttrName), listTypes),
        ),
        // Concat takes lists of same type and returns a list
        concatRule.rule to EqualAttributeValueConstraintGenerator(setOf(retTypeAttrName, "4.${retTypeAttrName}"), listTypes).and(
            BasicConstraintGenerator(listOf(isListConstraint(retTypeAttrName))),
        ),

        // len needs a list
        lenRule.rule to BasicConstraintGenerator(listOf(isListConstraint("2.${retTypeAttrName}"))),

        // contains takes a list
        listContainsRule.rule to BasicConstraintGenerator(listOf(isListConstraint("2.${retTypeAttrName}"))),

        // min/max needs int list
        minOrMaxRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", intListType)))),

        // Indexers need integer indicies
        indexIntoRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", intType)),
            isListConstraint("0.${retTypeAttrName}"))),
        indexIntoRangeRule.rule to BasicConstraintGenerator(listOf(BasicRuleConstraint(NodeAttribute("2.${retTypeAttrName}", intType)),
            BasicRuleConstraint(NodeAttribute("4.${retTypeAttrName}", intType)),
            isListConstraint(retTypeAttrName)
        )),

        // Int2bools and int2ints need int inputs.
        int2BoolRule.rule to BasicConstraintGenerator(listOf(
            BasicRuleConstraint(NodeAttribute("5.${retTypeAttrName}", intType)),
            BasicRuleConstraint(NodeAttribute("1.${retTypeAttrName}", intType))
        )),

        int2IntRule.rule to BasicConstraintGenerator(listOf(
            BasicRuleConstraint(NodeAttribute("5.${retTypeAttrName}", intType)),
            BasicRuleConstraint(NodeAttribute("1.${retTypeAttrName}", intType))
        )),

        // And bool2bool needs bools.
        bool2BoolRule.rule to BasicConstraintGenerator(listOf(
            BasicRuleConstraint(NodeAttribute("5.${retTypeAttrName}", boolType)),
            BasicRuleConstraint(NodeAttribute("1.${retTypeAttrName}", boolType))
        )),
    ), scopeCloserRules = setOf(
        totalRootLambdaRule.rule
    ))
}