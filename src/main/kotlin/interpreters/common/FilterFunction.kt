package interpreters.common

import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammars.common.rules.KeyedAttributesProductionRule
import grammars.common.rules.SynthesizeAttributeProductionRule
import languages.TypedFunctionalLanguage

class FilterFunction(val listType : String, val boolType : String) : HigherOrderFunctionExecutor(listOf(anyType), listOf(listType)) {
    override fun makeLambdaReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        return SynthesizeAttributeProductionRule(mapOf(language.typeAttr to language.argIdxToChild(1)), pr)
    }

    override fun execute(interpreter: (Any, List<Any>) -> Any, args: List<Any>): Any {
        val inList = castToType<List<Any>>(args[1], listType)
        return inList.filter {
            interpreter(args[0], listOf(it)) as Boolean
        }
    }

    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        return super.makeConstraints(language).and(
            BasicConstraintGenerator(makeConstraintFromType(language.typeAttr, boolType, language)) // The lambda needs to return a bool
        )
    }
}