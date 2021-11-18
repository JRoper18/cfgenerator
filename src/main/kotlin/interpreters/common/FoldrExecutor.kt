package interpreters.common

import grammar.ProductionRule
import grammars.common.rules.KeyedAttributesProductionRule
import grammars.common.rules.SynthesizeAttributeProductionRule
import languages.TypedFunctionalLanguage

class FoldrExecutor(val listType : String) : HigherOrderFunctionExecutor(listOf(anyType, anyType), listOf(anyType, listType)) {
    override fun makeLambdaReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        // for Foldr (f e list) return e
        return SynthesizeAttributeProductionRule(mapOf(language.typeAttr to language.argIdxToChild(1)), pr)
    }

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val inList = castToType<List<Any>>(args[2], listType)
        val acc = args[1]
        if(inList.isEmpty()) {
            return acc
        }
        else {
            // foldr f e cons(x, y) = f (foldr f e y) x
            val inner = FoldrExecutor(listType).execute(interpreter, listOf(
                args[0],
                acc,
                inList.subList(1, inList.size)))
            return interpreter(args[0], listOf(inner, inList[0]))

        }
    }
}