package interpreters.common.executors

import grammar.constraints.ConstraintGenerator
import grammar.constraints.EqualAttributeValueConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import languages.TypedFunctionalLanguage

class InsertExecutor(val listType : String, val intType : String, listTypeMapper: SingleAttributeMapper) : TypeMutatingFunctionExecutor(listOf(listType, intType, anyType), listTypeMapper, 2) {
    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val list = castToType<List<Any>>(args[0], listType)
        val idx = castToType<Int>(args[1], intType)
        val newEle = castToType<Any>(args[2], anyType)
        for(item in list) {
            if(item::class != newEle::class) {
                throw TypedFunctionalLanguage.TypeError(newEle::class.toString())
            }
        }
        return list.subList(0, idx) + newEle + list.subList(idx, list.size)
    }

    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        return super.makeConstraints(language).and(
            EqualAttributeValueConstraintGenerator(setOf(language.typeAttr, language.ithChildTypeKey(0)), language.flattenedComplexTypes[listType]!!)
        )
    }
}