package grammar

open class StringsetSymbol(val stringset: Set<String>, displayName : String = stringset.joinToString("|") ) : Symbol(false, displayName) {

}