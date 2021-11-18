package interpreters.common

import grammar.NodeAttribute
import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.ConstraintGenerator
import grammars.common.rules.*
import languages.TypedFunctionalLanguage

abstract class HigherOrderFunctionExecutor(val lambdaArgTypes : List<String>, val otherArgs : List<String>) : FunctionExecutor(
    otherArgs.size + 1
) {

    protected fun lambdaArgAttrKey(argIdx : Int, typeAttr: String) : String {
        return "lambdaArgs.$argIdx.${typeAttr}"
    }

    protected fun lambdaNumArgsAttrKey() : String {
        return "lambdaArgs.length"
    }

    abstract fun makeLambdaReturnTypeAPR(language : TypedFunctionalLanguage, pr : ProductionRule) : KeyedAttributesProductionRule

    override fun makeReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule,
    ): KeyedAttributesProductionRule {
        var apr = makeLambdaReturnTypeAPR(language, pr).withOtherRule {
            KeyChangeAttributeRule(pr, "length", language.argIdxToChild(0), lambdaNumArgsAttrKey())
        }
        lambdaArgTypes.forEachIndexed { index, argType ->
            val lambdaKey = language.ithLambdaArgTypeToKey(index)
            apr = apr.withOtherRule(KeyChangeAttributeRule(pr, lambdaKey, language.argIdxToChild(0), lambdaArgAttrKey(index, language.typeAttr)))
        }
        return apr
    }
    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        val constraints = otherArgs.flatMapIndexed { index, type ->
            if(type == anyType || index == 0) {
                listOf() // Ignore 0 index, that's the lambda
            }
            else {
                listOf(makeConstraintFromType(language, index + 1, type))
            }
        } + lambdaArgTypes.flatMapIndexed { index, type ->
            if(type == anyType) {
                listOf()
            }
            else {
                listOf(makeConstraintFromType(lambdaArgAttrKey(index, language.typeAttr), type, language))
            }
        } + BasicRuleConstraint(NodeAttribute(lambdaNumArgsAttrKey(), lambdaArgTypes.size.toString()))
        return BasicConstraintGenerator(constraints)
    }
}