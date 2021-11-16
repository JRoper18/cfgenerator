package grammars.lambda2

import grammar.NodeAttribute
import grammar.StringsetSymbol
import grammars.common.interpreters.IndexIntoFunction
import grammars.common.interpreters.MaxFunction
import grammars.common.interpreters.MinFunction
import grammars.common.interpreters.TypedFunctionalLanguage
import grammars.common.mappers.WrapperAttributeMapper
import grammars.common.rules.intSymbols

class Lambda2FunctionalLanguage {
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
    
    val interp = TypedFunctionalLanguage(
        basicTypesToValues = mapOf(intType to intSymbols(-1, 5), boolType to setOf("True", "False")),
        complexTypes = mapOf(listType to listTypeMapper),
        varName = varnames,
    functions = mapOf(
        "min" to MinFunction(intType, intListType),
        "max" to MaxFunction(intType, intListType),
        "indexinto" to IndexIntoFunction(listType, intType, listTypeMapper),
    ))

}