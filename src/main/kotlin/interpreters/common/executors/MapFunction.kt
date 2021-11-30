package interpreters.common.executors

import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.KeyedAttributesProductionRule
import languages.TypedFunctionalLanguage

class MapFunction(val lambdaType : String, val listType : String, val listTypeMapper: SingleAttributeMapper) : HigherOrderFunctionExecutor(listOf(
    anyType
), listOf(listType)) {
    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val inList = castToType<List<Any>>(args[1], listType)
        return inList.map {
            interpreter(args[0], listOf(it))
        }
    }

    override fun makeHigherOrderReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        // Returns a list of the lambda's output
        return AttributeMappingProductionRule(pr, language.typeAttr, language.argIdxToChild(0), listTypeMapper)
    }

}