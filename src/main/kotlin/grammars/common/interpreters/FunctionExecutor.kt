package grammars.common.interpreters

import grammar.GenericGrammarNode
import grammar.NodeAttribute
import grammar.ProductionRule
import grammar.constraints.*
import grammars.common.rules.KeyedAttributesProductionRule

abstract class FunctionExecutor(val numArgs : Int) {
    companion object {
        const val anyType = "any"
    }
    inline fun <reified T> castToType(arg : Any, wantedType : String) : T {
        if(!(arg is T)) {
            throw TypedFunctionalLanguage.TypeError(wantedType = wantedType)
        }
        return arg
    }

    fun checkArgs(args : List<Any>) {
        if(args.size != numArgs){
            throw TypedFunctionalLanguage.ParseError("Function expects ${numArgs} args")
        }
    }
    protected fun makeConstraintFromType(language: TypedFunctionalLanguage, idx : Int, type : String) : RuleConstraint {
        return makeConstraintFromType(language.ithChildTypeKey(idx), type, language.basicTypesToValues.keys, language.flattenedComplexTypes)
    }
    protected fun makeConstraintFromType(consKey : String, type : String,
                                         basicTypes : Set<String>,
                                         flattenedComplexTypes : Map<String, Collection<String>>) : RuleConstraint {
        require(type != anyType) {
            "Anytype doesn't make a constraint!"
        }
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
                // It's specific, but double check:
                check(type in flattenedComplexTypes.values.flatten()){
                    "Unrecognized type $type"
                }
                return BasicRuleConstraint(NodeAttribute(consKey, type))
            }
        }
    }
    abstract fun makeConstraints(language : TypedFunctionalLanguage) : ConstraintGenerator
    abstract fun makeReturnTypeAPR(language: TypedFunctionalLanguage, pr: ProductionRule) : KeyedAttributesProductionRule
    abstract fun execute(interpreter: (GenericGrammarNode, List<Any>) -> Any, args: List<Any>) : Any
}