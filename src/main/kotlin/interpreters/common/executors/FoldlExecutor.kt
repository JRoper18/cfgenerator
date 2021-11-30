package interpreters.common.executors

import grammar.ProductionRule
import grammars.common.rules.KeyedAttributesProductionRule
import grammars.common.rules.SynthesizeAttributeProductionRule
import languages.TypedFunctionalLanguage

class FoldlExecutor(val listType : String) : HigherOrderFunctionExecutor(listOf(anyType, anyType), listOf(anyType, listType)) {
    override fun makeHigherOrderReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        // for Foldl (f e list) return e
        return SynthesizeAttributeProductionRule(mapOf(language.typeAttr to language.argIdxToChild(1)), pr)
    }

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val inList = castToType<List<Any>>(args[2], listType)
        val acc = args[1]
        if(inList.isEmpty()) {
            return acc
        }
        else {
            // foldl f e cons(x, y) = foldl f (f e x) y
            val folded = interpreter(args[0], listOf(acc, inList[0]))
            return FoldlExecutor(listType).execute(interpreter, listOf(
                args[0],
                folded,
                inList.subList(1, inList.size)))
        }
    }
}