package interpreters.common.executors

import grammar.constraints.ConstraintGenerator
import grammar.constraints.EqualAttributeValueConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import languages.TypedFunctionalLanguage

class ConsFunction(val listType : String, val listTypeMapper : SingleAttributeMapper): TypeMutatingFunctionExecutor(listOf(
    anyType, listType), listTypeMapper, 0) {

    override fun makeConstraints(
        language: TypedFunctionalLanguage,
    ): ConstraintGenerator {
        return super.makeConstraints(language).and(
        EqualAttributeValueConstraintGenerator(setOf(language.ithChildTypeKey(1), language.typeAttr),
            language.flattenedComplexTypes[listType]!!)
        )
    }
    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
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