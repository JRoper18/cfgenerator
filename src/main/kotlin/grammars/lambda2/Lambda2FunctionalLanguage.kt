package grammars.lambda2

import grammar.NodeAttribute
import grammar.StringsetSymbol
import grammars.common.interpreters.*
import grammars.common.mappers.WrapperAttributeMapper
import grammars.common.rules.intSymbols

object Lambda2FunctionalLanguage {
    val intType = "int"
    val boolType = "bool"
    val listType = "list"
    val listTypeMapper = WrapperAttributeMapper()
    val intListType = listTypeMapper.forward(intType)
    val intListListType = listTypeMapper.forward(intListType)

    val varnames = StringsetSymbol(mapOf(
        // Int variables
        "i" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intType)),
        "j" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intType)),
        "k" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intType)),
        "l" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intType)),
        "m" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intType)),
        "n" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intType)),
        // Bool variables
        "o" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, boolType)),
        "p" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, boolType)),
        "q" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, boolType)),
        "r" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, boolType)),
        // Int list lists
        "x" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListListType)),
        "y" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListListType)),
        "z" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListListType)),
        "a" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListListType)),
        "b" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListListType)),
        "c" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListListType)),
        // Int lists
        "s" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListType)),
        "t" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListType)),
        "u" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListType)),
        "v" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListType)),
        "w" to setOf(NodeAttribute(TypedFunctionalLanguage.typeAttr, intListType)),
    ))
    
    val language = TypedFunctionalLanguage(
        basicTypesToValues = mapOf(intType to IntRange(-1, 5).toSet(), boolType to setOf(true, false)),
        complexTypes = mapOf(listType to listTypeMapper),
        varName = varnames,
    functions = mapOf(
        "min" to MinFunction(intType, intListType),
        "max" to MaxFunction(intType, intListType),
        "indexinto" to IndexIntoFunction(listType, intType, listTypeMapper),
        "cons" to ConsFunction(listType, listTypeMapper),
        "concat" to ConcatFunction(listType),
        "length" to LengthFunction(listType, intType),
    ))

}