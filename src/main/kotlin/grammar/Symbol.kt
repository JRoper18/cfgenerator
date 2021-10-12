package grammar

import java.util.regex.Pattern

sealed class Symbol(open val terminal: Boolean = false,
                  val name: String
){
    override fun toString(): String {
        if(!terminal) {
            return "<$name>"
        }
        else {
            return "\"${toEscapedString()}\""
        }
    }
    fun toEscapedString() : String {
        return name.replace("\n", "\\n").replace("\t", "\\t")
    }
    fun toRegexEscapedString() : String {
        return toEscapedString().replace("[", "\\[").replace("]", "\\]").replace("|", "\\|")
    }
    fun toAntlrRuleName() : String {
        return name.replace(Regex("[^A-Za-z0-9_]"), "").replaceFirstChar {
            it.lowercaseChar() //Rules start with a lowercase character
        }
    }
    companion object {
        val specialCharsToNames = mapOf<String, String>(
            "\n" to "NEWLINE",
            "\t" to "TAB",
            " " to "SPACE",
            ":" to "COLON",
            "=" to "EQUALS",
            "[" to "LB",
            "]" to "RB",
            "%" to "PERCENT",
            "(" to "LP",
            ")" to "RP",
            "-" to "DASH",
            "*" to "TIMES",
            "+" to "PLUS",
            "<" to "LT",
            ">" to "GT",
            "/" to "FSLASH",
            "\\" to "BSLASH"
        )
    }
    fun toAntlrLexerName() : String {
        var replaced = name
        specialCharsToNames.forEach {
            replaced = replaced.replace(it.key, it.value)
        }
        return replaced.replaceFirstChar {
            it.uppercaseChar()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Symbol

        if (terminal != other.terminal) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = terminal.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }


}