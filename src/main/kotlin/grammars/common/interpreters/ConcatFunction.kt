package grammars.common.interpreters

import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammar.constraints.EqualAttributeValueConstraintGenerator
import grammars.common.mappers.IdentityMapper

class ConcatFunction(val listType : String) : TypeMutatingFunctionExecutor(listOf(listType, listType), IdentityMapper(), 0) {
    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        return EqualAttributeValueConstraintGenerator(setOf(language.ithChildTypeKey(0), language.ithChildTypeKey(1)), language.flattenedComplexTypes[listType]!!).and(
            BasicConstraintGenerator(listOf(makeConstraintFromType(language, 0, listType))),
        )
    }

    override fun execute(args: List<Any>): Any {
        return castToType<List<Any>>(args[0], listType) + castToType<List<Any>>(args[1], listType)
    }

}