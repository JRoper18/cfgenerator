package grammar

open class StringsetSymbol(val stringset: List<String>, displayName : String = stringset.joinToString("|") ) : Symbol(false, displayName) {

}