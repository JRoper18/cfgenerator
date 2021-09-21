package generators

import grammar.GenericGrammarNode
import grammar.RootGrammarNode

class ProgramStringifier {
    fun stringify(program: GenericGrammarNode) : String{
        // Just left-explore the tree
        val build = StringBuilder()
        val symb = program.lhsSymbol()
        if(symb.terminal) {
            build.append(symb.name)
        }
        program.rhs.forEach {
            build.append(stringify(it))
        }
        return build.toString()
    }
}