package grammars.common.interpreters

import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammar.constraints.EqualAttributeValueConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.KeyedAttributesProductionRule
import grammars.lambda2.Lambda2Grammar

class ConsFunction(val listType : String, val listTypeMapper : SingleAttributeMapper): TypeMutatingFunctionExecutor(listOf(anyType, listType), listTypeMapper, 0) {

    override fun makeConstraints(
        language: TypedFunctionalLanguage,
    ): ConstraintGenerator {
        return super.makeConstraints(language).and(
        EqualAttributeValueConstraintGenerator(setOf(language.ithChildTypeKey(1), TypedFunctionalLanguage.typeAttr),
            language.flattenedComplexTypes[listType]!!)
        )
    }
    override fun execute(args: List<Any>): Any {
        val single = args[0]
        val list = castToType<List<Any>>(args[1], listType)
        list.forEach {
            require(it.javaClass == single.javaClass) {
                "Cons takes a type and list of that type. "
            }
        }
        return listOf(single) + list
    }
}