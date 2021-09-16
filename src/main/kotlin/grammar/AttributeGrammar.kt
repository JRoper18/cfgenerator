package grammar

class AttributeGrammar(givenRules: List<AttributedProductionRule>, val start : Symbol){
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
                ret = symbol.stringset.map {
                    APR(PR(symbol, listOf(StringSymbol(it))))
                }
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
    }

    fun getPossibleExpansions(lhs: Symbol): List<AttributedProductionRule>{
        return this.expansions.getOrDefault(lhs, listOf())
    }

}