package generators

import grammar.GenericGrammarNode
import grammar.RootGrammarNode

class ProgramStringifier(val tokenSeperator : String = "") {
    fun stringify(program: GenericGrammarNode) : String{
        // Just left-explore the tree
        val build = StringBuilder()
        val symb = program.lhsSymbol()
        if(symb.terminal) {
            build.append(symb.name)
        }
        program.rhs.forEach {
            build.append(stringify(it))
            build.append(tokenSeperator)
        }
        return build.toString()
    }
}