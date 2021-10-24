package grammar

class StringSymbol(str: String) : Symbol(true, str) {
    companion object {
        fun fromPrintedString(str : String) : StringSymbol {
            return StringSymbol(str.replace("\"", ""))
        }
    }
}