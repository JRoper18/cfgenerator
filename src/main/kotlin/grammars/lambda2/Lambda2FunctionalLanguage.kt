package grammars.lambda2

import grammar.NodeAttribute
import grammar.StringsetSymbol
import grammars.common.interpreters.*
import grammars.common.mappers.WrapperAttributeMapper
import grammars.common.rules.intSymbols
import java.util.logging.Filter

object Lambda2FunctionalLanguage {
    val intType = "int"
    val boolType = "bool"
    val listType = "list"
    val listTypeMapper = WrapperAttributeMapper()
    val intListType = listTypeMapper.forward(intType)
    val intListListType = listTypeMapper.forward(intListType)
    val typeAttr = "type"
    val varnames = StringsetSymbol(mapOf(
        // Int variables
        "i" to setOf(NodeAttribute(typeAttr, intType)),
        "j" to setOf(NodeAttribute(typeAttr, intType)),
        "k" to setOf(NodeAttribute(typeAttr, intType)),
        "l" to setOf(NodeAttribute(typeAttr, intType)),
        "m" to setOf(NodeAttribute(typeAttr, intType)),
        "n" to setOf(NodeAttribute(typeAttr, intType)),
        // Bool variables
        "o" to setOf(NodeAttribute(typeAttr, boolType)),
        "p" to setOf(NodeAttribute(typeAttr, boolType)),
        "q" to setOf(NodeAttribute(typeAttr, boolType)),
        "r" to setOf(NodeAttribute(typeAttr, boolType)),
        // Int list lists
        "x" to setOf(NodeAttribute(typeAttr, intListListType)),
        "y" to setOf(NodeAttribute(typeAttr, intListListType)),
        "z" to setOf(NodeAttribute(typeAttr, intListListType)),
        "a" to setOf(NodeAttribute(typeAttr, intListListType)),
        "b" to setOf(NodeAttribute(typeAttr, intListListType)),
        "c" to setOf(NodeAttribute(typeAttr, intListListType)),
        // Int lists
        "s" to setOf(NodeAttribute(typeAttr, intListType)),
        "t" to setOf(NodeAttribute(typeAttr, intListType)),
        "u" to setOf(NodeAttribute(typeAttr, intListType)),
        "v" to setOf(NodeAttribute(typeAttr, intListType)),
        "w" to setOf(NodeAttribute(typeAttr, intListType)),
    ))
    val lambdaType = "lambda"
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
        "len" to LengthFunction(listType, intType),
        "map" to MapFunction(lambdaType, listType, listTypeMapper),
        "filter" to FilterFunction(listType, boolType, lambdaType),
        "plus" to BinaryInt2IntExecutor(BinaryInt2IntExecutor.Operation.PLUS, intType),
        "minus" to BinaryInt2IntExecutor(BinaryInt2IntExecutor.Operation.MINUS, intType),
        "lt" to BinaryInt2BoolExecutor(BinaryInt2BoolExecutor.Operation.LT, intType, boolType),
        "gt" to BinaryInt2BoolExecutor(BinaryInt2BoolExecutor.Operation.GT, intType, boolType),
        "neg" to NegationExecutor(boolType),
        "equals" to EqualsExecutor(),
        "contains" to ContainsRule(listType, boolType),
        "or" to BinaryBool2BoolExecutor(BinaryBool2BoolExecutor.Operation.OR, boolType),
        "and" to BinaryBool2BoolExecutor(BinaryBool2BoolExecutor.Operation.AND, boolType)
    ),
    typeAttr = typeAttr)

}