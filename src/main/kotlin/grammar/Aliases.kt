package grammar

typealias NtSym = NonterminalSymbol
typealias GN = GrammarNode
typealias PR = ProductionRule
typealias APR = AttributedProductionRule

fun grammar(initializer: AttributeGrammar.() -> Unit): AttributeGrammar {
    return AttributeGrammar().apply(initializer)
}