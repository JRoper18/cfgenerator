package grammar

data class ProductionRule(val lsh: Symbol, val rhs: List<Symbol>) {
    override fun toString(): String {
        return "${lsh} -> ${rhs}"
    }
}