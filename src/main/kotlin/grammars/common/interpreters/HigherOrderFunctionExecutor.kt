package grammars.common.interpreters

import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.InitAttributeProductionRule
import grammars.common.rules.KeyChangeAttributeRule
import grammars.common.rules.KeyedAttributesProductionRule

abstract class HigherOrderFunctionExecutor(val lambdaArgTypes : List<String>, val otherArgs : List<String>) : FunctionExecutor(
    otherArgs.size + 1
) {

    protected fun lambdaArgAttrKey(argIdx : Int, typeAttr: String) : String {
        return "lambdaArgs.$argIdx.${typeAttr}"
    }

    abstract fun makeLambdaReturnTypeAPR(language : TypedFunctionalLanguage, pr : ProductionRule) : KeyedAttributesProductionRule

    override fun makeReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule,
    ): KeyedAttributesProductionRule {
        var apr = makeLambdaReturnTypeAPR(language, pr)
        lambdaArgTypes.forEachIndexed { index, argType ->
            val lambdaKey = language.ithLambdaArgTypeToKey(index)
            apr = apr.withOtherRule(KeyChangeAttributeRule(pr, lambdaKey, language.argIdxToChild(0), lambdaArgAttrKey(index, language.typeAttr)))
        }
        return apr
    }
    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        val constraints = otherArgs.flatMapIndexed { index, type ->
            if(type == anyType || index == 0) {
                listOf()
            }
            else {
                listOf(makeConstraintFromType(language, index + 1, type))
            }
        } + lambdaArgTypes.flatMapIndexed { index, type ->
            if(type == anyType) {
                listOf()
            }
            else {
                listOf(makeConstraintFromType(lambdaArgAttrKey(index, language.typeAttr), type, language.basicTypesToValues.keys, language.flattenedComplexTypes))
            }
        }
        return BasicConstraintGenerator(constraints)
    }
}