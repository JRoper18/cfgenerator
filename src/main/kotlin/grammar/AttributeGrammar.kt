package grammar

class AttributeGrammar(rules: List<AttributedProductionRule>, val start : Symbol){
    // Maps LHS symbols to a list of possible RHS symbol lists.
    val expansions: Map<Symbol, List<AttributedProductionRule>> = rules.groupBy {
        it.rule.lsh
    }

    fun getPossibleExpansions(lhs: Symbol): List<AttributedProductionRule>{
        return this.expansions.getOrDefault(lhs, listOf())
    }

}