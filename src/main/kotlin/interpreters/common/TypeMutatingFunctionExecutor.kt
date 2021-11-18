package interpreters.common

import grammar.ProductionRule
import grammar.constraints.BasicConstraintGenerator
import grammar.constraints.ConstraintGenerator
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.AttributeMappingProductionRule
import grammars.common.rules.KeyedAttributesProductionRule
import languages.TypedFunctionalLanguage

/**
 * When determining return type, it's a mutation/map from an initial type of the child at the index.
 */
abstract class TypeMutatingFunctionExecutor(val inTypes : List<String>,
                                            val outTypeMapper : SingleAttributeMapper,
                                            val argTypeIdx : Int) :
    FunctionExecutor(inTypes.size) {

    override fun makeConstraints(language: TypedFunctionalLanguage): ConstraintGenerator {
        val constraints = inTypes.flatMapIndexed { index, type ->
            if(type == anyType) {
                listOf()
            }
            else if(index == argTypeIdx) {
                listOf()
            }
            else {
                listOf(makeConstraintFromType(language, index, type))
            }
        }
        return BasicConstraintGenerator(constraints)
    }
    override fun makeReturnTypeAPR(language: TypedFunctionalLanguage, pr : ProductionRule): KeyedAttributesProductionRule {
        return AttributeMappingProductionRule(pr, language.typeAttr, language.argIdxToChild(argTypeIdx), outTypeMapper)
    }
}