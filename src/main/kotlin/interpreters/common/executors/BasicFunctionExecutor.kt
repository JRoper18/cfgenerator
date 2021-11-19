package interpreters.common.executors

import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammars.common.rules.InitAttributeProductionRule
import grammars.common.rules.KeyedAttributesProductionRule
import languages.TypedFunctionalLanguage

abstract class BasicFunctionExecutor(val inTypes : List<String>, val outType : String) : FunctionExecutor(inTypes.size) {
    override fun makeReturnTypeAPR(language : TypedFunctionalLanguage, pr: ProductionRule): KeyedAttributesProductionRule {
        return InitAttributeProductionRule(pr, language.typeAttr, outType)
    }
    override fun makeConstraints(language : TypedFunctionalLanguage) : ConstraintGenerator {
        val constraints = inTypes.flatMapIndexed { index, type ->
            if(type == anyType) {
                listOf()
            }
            else {
                listOf(makeConstraintFromType(language, index, type))
            }
        }
        return BasicConstraintGenerator(constraints)
    }
}