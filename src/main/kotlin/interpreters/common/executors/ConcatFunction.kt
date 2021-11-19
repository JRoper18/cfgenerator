package interpreters.common.executors

import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammar.constraints.EqualAttributeValueConstraintGenerator
import grammars.common.mappers.IdentityMapper
import languages.TypedFunctionalLanguage
import utils.cartesian

class ConcatFunction(val listType : String) : TypeMutatingFunctionExecutor(listOf(listType, listType), IdentityMapper(), 0) {
    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        return EqualAttributeValueConstraintGenerator(setOf(language.ithChildTypeKey(0), language.ithChildTypeKey(1)), language.flattenedComplexTypes[listType]!!).and(
            BasicConstraintGenerator(listOf(makeConstraintFromType(language, 0, listType))),
        )
    }

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val l1 = castToType<List<Any>>(args[0], listType)
        val l2 = castToType<List<Any>>(args[1], listType)
        for(pair in l1.cartesian(l2)) {
            if(!(pair.first::class == pair.second::class)) {
                throw TypedFunctionalLanguage.InterpretError("Concating lists of different types: $pair")
            }
        }
        return l1 + l2
    }

}