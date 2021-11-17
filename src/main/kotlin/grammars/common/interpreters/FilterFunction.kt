package grammars.common.interpreters

import grammar.GenericGrammarNode
import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.BasicRuleConstraint
import grammar.constraints.ConstraintGenerator
import grammar.constraints.RuleConstraint
import grammars.common.mappers.IdentityMapper
import grammars.common.rules.KeyedAttributesProductionRule
import grammars.common.rules.SynthesizeAttributeProductionRule

class FilterFunction(val listType : String, val boolType : String, val lambdaType : String) : HigherOrderFunctionExecutor(listOf(anyType), listOf(listType)) {
    override fun makeLambdaReturnTypeAPR(
        language: TypedFunctionalLanguage,
        pr: ProductionRule
    ): KeyedAttributesProductionRule {
        return SynthesizeAttributeProductionRule(mapOf(language.typeAttr to language.argIdxToChild(1)), pr)
    }

    override fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>): Any {
        val lambdaProgNode = castToType<GenericGrammarNode>(args[0], lambdaType)
        val inList = castToType<List<Any>>(args[1], listType)
        return inList.filter {
            interpreter(lambdaProgNode, listOf(it)) as Boolean
        }
    }

    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        return super.makeConstraints(language).and(
            BasicConstraintGenerator(makeConstraintFromType(language.typeAttr, boolType, language)) // The lambda needs to return a bool
        )
    }
}