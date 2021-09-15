package grammar

open class StringsetSymbol(val stringset: List<String>) : Symbol(true, stringset.joinToString("|")) {

}