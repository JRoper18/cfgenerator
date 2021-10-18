package grammar

open class ProductionRule(val lhs: Symbol, val rhs: List<Symbol>) {
    override fun toString(): String {
        return "${lhs} -> ${rhs.joinToString(" ")}"
    }

    fun isRecursive() : Boolean {
        return this.rhs.contains(this.lhs)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductionRule

        if (lhs != other.lhs) return false
        if (rhs != other.rhs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lhs.hashCode()
        result = 31 * result + rhs.hashCode()
        return result
    }


}