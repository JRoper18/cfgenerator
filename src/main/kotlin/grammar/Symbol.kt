package grammar

sealed class Symbol(open val terminal: Boolean = false,
                  val name: String
){
    override fun toString(): String {
        if(!terminal) {
            return "<$name>"
        }
        else {
            return "\"$name\"".replace("\n", "\\n").replace("\t", "\\t")
        }
    }
}