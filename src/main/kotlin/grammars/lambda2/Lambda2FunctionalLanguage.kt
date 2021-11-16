package grammars.lambda2

import grammar.NodeAttribute
import grammar.StringsetSymbol
import grammars.common.interpreters.IndexIntoFunction
import grammars.common.interpreters.MaxFunction
import grammars.common.interpreters.MinFunction
import grammars.common.interpreters.TypedFunctionalInterpreter
import grammars.common.mappers.WrapperAttributeMapper

class Lambda2FunctionalLanguage {
    val intType = "int"
    val boolType = "bool"
    val listType = "list"
    val listTypeMapper = WrapperAttributeMapper()
    val intListType = listTypeMapper.forward(intType)
    val intListListType = listTypeMapper.forward(intListType)

    val varnames = StringsetSymbol(mapOf(
        // Int variables
        "i" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intType)),
        "j" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intType)),
        "k" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intType)),
        "l" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intType)),
        "m" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intType)),
        "n" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intType)),
        // Bool variables
        "o" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, boolType)),
        "p" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, boolType)),
        "q" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, boolType)),
        "r" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, boolType)),
        // Int list lists
        "x" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListListType)),
        "y" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListListType)),
        "z" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListListType)),
        "a" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListListType)),
        "b" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListListType)),
        "c" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListListType)),
        // Int lists
        "s" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListType)),
        "t" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListType)),
        "u" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListType)),
        "v" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListType)),
        "w" to setOf(NodeAttribute(TypedFunctionalInterpreter.typeAttr, intListType)),
    ))
    
    val interp = TypedFunctionalInterpreter(basicTypes = setOf(intType, boolType), complexTypes = mapOf(listType to listTypeMapper), varName = varnames,
    functions = mapOf(
        "min" to MinFunction(intType, listTypeMapper.forward(intType)),
        "max" to MaxFunction(intType, listTypeMapper.forward(intType)),
        "indexinto" to IndexIntoFunction(listType, intType, listTypeMapper),
    ))

}