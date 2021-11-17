package grammars.common.interpreters

import grammar.GenericGrammarNode
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper

class MapFunction(val lambdaType : String, val listType : String, listTypeMapper: SingleAttributeMapper) : HigherOrderFunctionExecutor(listOf("any"), listTypeMapper, listOf(listType)) {
    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val lambdaProgNode = castToType<GenericGrammarNode>(args[0], lambdaType)
        val inList = castToType<List<Any>>(args[1], listType)
        return inList.map {
            interpreter(lambdaProgNode, listOf(it))
        }
    }

    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        return BasicConstraintGenerator(makeConstraintFromType(lambdaArgAttrKey(0, language.typeAttr), listType, language.basicTypesToValues.keys, language.flattenedComplexTypes))
    }
}