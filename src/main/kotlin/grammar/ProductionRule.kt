package grammar

data class ProductionRule(val lhs: Symbol, val rhs: List<Symbol>) {
    override fun toString(): String {
        return "${lhs} -> ${rhs.joinToString(" ")}"
    }

    fun isRecursive() : Boolean {
        return this.rhs.contains(this.lhs)
    }
}