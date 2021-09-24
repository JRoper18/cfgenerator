package grammar

class NonterminalSymbol(name: String): Symbol(false, name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return name == (other as NonterminalSymbol).name
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}
