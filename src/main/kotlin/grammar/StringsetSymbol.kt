package grammar

open class StringsetSymbol(val stringAttributeSet: Map<String, Set<NodeAttribute>>, val attributeName : String = "chosenSymbol", displayName : String = stringAttributeSet.keys.joinToString("|") ) : Symbol(false, displayName) {
    val stringset = stringAttributeSet.keys
    constructor(stringset: Set<String>, attributeName : String = "chosenSymbol", displayName : String = stringset.joinToString("|") ):
            this(stringset.map{
                Pair(it, setOf<NodeAttribute>())
            }.toMap(), attributeName = attributeName, displayName = displayName)
}