package grammar

import grammar.constraints.ConstraintGenerator
import grammars.common.makeStringsetRules

class AttributeGrammar(givenRules: List<AttributedProductionRule>, val constraints : Map<AttributedProductionRule, ConstraintGenerator>, val start : Symbol){
    // Maps LHS symbols to a list of possible RHS symbol lists.
    val expansions: Map<Symbol, List<AttributedProductionRule>> by lazy {
        this.rules.groupBy {
            it.rule.lsh
        }
    }

    val symbols: List<Symbol> = givenRules.flatMap {
        it.rule.rhs + it.rule.lsh
    }

    val rules : List<AttributedProductionRule> = givenRules + symbols.flatMap { symbol ->
        var ret = listOf<AttributedProductionRule>()
        when(symbol) {
            is StringsetSymbol -> {
                ret = makeStringsetRules(symbol)
            }
            is NonterminalSymbol -> {
                //Ignore
            }
            is StringSymbol -> {
                //No rules here either.
            }
        }
        ret
    }

    init {
        // Validate the grammar. Every non-terminal symbol should have an expansion.
        symbols.forEach {
            require(it.terminal || expansions.containsKey(it)) {
                "Symbol ${it.name} needs expansion rules"
            }
        }
        // Validate that each constraint actually correlates with a rule.
        constraints.forEach {
            require(givenRules.contains(it.key)) {
                "Rule ${it.key} is not present in the grammar but has a constraint."
            }
        }
    }

    fun getPossibleExpansions(lhs: Symbol): List<AttributedProductionRule>{
        return this.expansions.getOrDefault(lhs, listOf())
    }

}