package grammar

open class StringsetSymbol(val stringAttributeSet: Map<String, Set<NodeAttribute>>, val attributeName : String = "chosenSymbol", displayName : String = stringAttributeSet.keys.joinToString("|") ) : Symbol(false, displayName) {
    val stringset = stringAttributeSet.keys
    constructor(stringset: Set<String>, attributeName : String = "chosenSymbol", displayName : String = stringset.joinToString("|") ):
            this(stringset.map{
                Pair(it, setOf<NodeAttribute>())
            }.toMap(), attributeName = attributeName, displayName = displayName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as StringsetSymbol

        if (stringAttributeSet != other.stringAttributeSet) return false
        if (attributeName != other.attributeName) return false
        if (stringset != other.stringset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + stringAttributeSet.hashCode()
        result = 31 * result + attributeName.hashCode()
        result = 31 * result + stringset.hashCode()
        return result
    }
}