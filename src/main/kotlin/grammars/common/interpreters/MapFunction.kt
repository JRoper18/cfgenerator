package grammars.common.interpreters

import grammar.GenericGrammarNode
import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammar.constraints.EqualAttributeValueConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.KeyedAttributesProductionRule

class MapFunction(val lambdaType : String, val listType : String, val listTypeMapper: SingleAttributeMapper) : HigherOrderFunctionExecutor(listOf(
    anyType), listOf(listType)) {
    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val lambdaProgNode = castToType<GenericGrammarNode>(args[0], lambdaType)
        val inList = castToType<List<Any>>(args[1], listType)
        return inList.map {
            interpreter(lambdaProgNode, listOf(it))
        }
    }

    override fun makeLambdaReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        // Returns a list of the lambda's output
        return AttributeMappingProductionRule(pr, language.typeAttr, language.argIdxToChild(0), listTypeMapper)
    }
}