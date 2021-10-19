package grammars.lambda2

class Lambda2Interpreter{
    internal class ParseError : IllegalArgumentException()

    fun interp(program: String, args : List<Any>) : String {
        val tr = (toTree(tokenize(program)))
        require(tr.data == "lambda") {
            "Root of a program must be a lambda"
        }
        val argList = tr.children[0].children.map {
            it.data
        }
        require(argList.size == args.size) {
            "Number of args into the program must match the number of args in the input sig"
        }
        val argMap = args.mapIndexed { index, any ->
            Pair(argList[index], any)
        }.toMap()
        val func = tr.children[1]
        return eval(func, argMap)
    }

    fun tokenize(string: String) : List<String> {
        return string.split(Regex("\\s")).filter {
            it.isNotBlank()
        }
    }

    internal fun eval(tree: Tree, vars : Map<String, Any> = mapOf()) : String {
        if(tree.children.isEmpty()) {
            return tree.data // A constant
        }
        val op = tree.data
        when(op) {
            "*" -> {
                return (eval(tree.children[0]).toInt() * eval(tree.children[1]).toInt()).toString()
            }
            "+" -> {
                return (eval(tree.children[0]).toInt() + eval(tree.children[1]).toInt()).toString()
            }
            "-" -> {
                return (eval(tree.children[0]).toInt() - eval(tree.children[1]).toInt()).toString()
            }
            ">" -> {
                return (eval(tree.children[0]).toInt() > eval(tree.children[1]).toInt()).toString()
            }
            "<" -> {
                return (eval(tree.children[0]).toInt() < eval(tree.children[1]).toInt()).toString()
            }
            "=" -> {
                return (eval(tree.children[0]).toInt() == eval(tree.children[1]).toInt()).toString()
            }
            "||" -> {
                return (eval(tree.children[0]).toBooleanStrict() || eval(tree.children[1]).toBooleanStrict()).toString()
            }
            "&&" -> {
                return (eval(tree.children[0]).toBooleanStrict() || eval(tree.children[1]).toBooleanStrict()).toString()
            }
            "lambda" -> {
                // Just a declaration, nothing to do here.
            }
            "foldt" -> {

            }
        }
        return "TODO"
    }


    internal data class Tree(var data : String = "",
                             var children : MutableList<Tree> = mutableListOf()) {
    }


    internal fun toTree(tokens: List<String>) : Tree {
        return toTreeParse(tokens).first
    }
    // Returns a tree and the last token parsed.
    internal fun toTreeParse(tokens : List<String>) : Pair<Tree, Int>{
        if(tokens.size == 1 || tokens[0] != "(") {
            val t = Tree()
            t.data = tokens[0]
            return Pair(t, 0)
        }
        else if(tokens.subList(0, 2) == listOf("(", ")")){
            return Pair(Tree(), 1)
        }
        else if(tokens.size == 3){
            // It's a LP, constant, RP
            val t = Tree()
            t.data = tokens[1]
            return Pair(t, 2)
        }
        else {
            val op = tokens[1]
            val t = Tree()
            t.data = op
            // Make the children.
            var i = 2
            while(i < tokens.size) {
                val token = tokens[i]
                if(token == "(") {
                    // Parse the subtree.
                    val subtokens = tokens.subList(i, tokens.size)
                    val subtreeRes = toTreeParse(subtokens)
                    i += subtreeRes.second + 1
                    t.children.add(subtreeRes.first)
                }
                else if(token == ")"){
                    // End the parse.
                    return Pair(t, i)
                }
                else {
                    // Not-parened constant or something.
                    val child = Tree()
                    child.data = tokens[i]
                    t.children.add(child)
                    i += 1
                }
            }
            return Pair(t, i)
        }
    }

}