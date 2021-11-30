package interpreters.common.executors

import grammar.ProductionRule
import grammars.common.rules.KeyedAttributesProductionRule
import grammars.common.rules.SynthesizeAttributeProductionRule
import languages.TypedFunctionalLanguage

class ReclExecutor(val listType : String) : HigherOrderFunctionExecutor(listOf(anyType, anyType), listOf(anyType, listType)) {
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
            // recl f e cons(x, y) = f x y
            val y : List<Any>
            if(inList.size == 1) {
                y = listOf()
            } else {
                y = inList.subList(1, inList.size)
                if(y[0]::class != inList[0]::class) {
                    throw TypedFunctionalLanguage.TypeError(y[0]::class.simpleName!!)
                }
            }
            return interpreter(args[0], listOf(inList[0], y))
        }
    }
}