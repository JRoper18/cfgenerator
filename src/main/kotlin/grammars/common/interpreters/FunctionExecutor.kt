package grammars.common.interpreters

import grammar.NodeAttribute
import grammar.ProductionRule
import grammar.constraints.*
import grammars.common.rules.KeyedAttributesProductionRule

abstract class FunctionExecutor(val inTypes : List<String>) {
    val numArgs = inTypes.size
    inline fun <reified T> castToType(arg : Any, wantedType : String) : T {
        if(!(arg is T)) {
            throw TypedFunctionalLanguage.TypeError(wantedType = wantedType)
        }
        return arg
    }

    fun checkArgs(args : List<Any>) {
        if(args.size != numArgs){
            throw TypedFunctionalLanguage.ParseError("Function expects ${inTypes.size} args")
        }
        args.forEachIndexed { index, any ->
            castToType(args[index], inTypes[index])
        }
    }
    protected fun makeConstraintFromType(consKey : String, type : String,
                                         basicTypes : Set<String>,
                                         flattenedComplexTypes : Map<String, List<String>>) : RuleConstraint {
        if(type in basicTypes) {
            return BasicRuleConstraint(NodeAttribute(consKey, type))
        }
        else { // It's a complex type, like an [int] or some other list.
            // Is it a specific complex type (we need a list of ints) or a generic (we just need any list)?
            val genericTypes = flattenedComplexTypes[type]
            if(genericTypes != null) {
                // It's generic.
                return OrRuleConstraint(genericTypes.map {
                    BasicRuleConstraint(NodeAttribute(consKey, it))
                })
            }
            else {
                // It's specific.
                return BasicRuleConstraint(NodeAttribute(consKey, type))
            }
        }
    }
    open fun makeConstraints(language : TypedFunctionalLanguage, basicTypes : Set<String>,
                             flattenedComplexTypes : Map<String, List<String>>) : ConstraintGenerator {
        val constraints = inTypes.mapIndexed { index, type ->
            makeConstraintFromType(language.ithChildTypeKey(index), type, basicTypes, flattenedComplexTypes)
        }
        return BasicConstraintGenerator(constraints)
    }
    abstract fun makeReturnTypeAPR(language : TypedFunctionalLanguage, pr : ProductionRule, typeAttr : String) : KeyedAttributesProductionRule
    abstract fun execute(args : List<Any>) : Any
}