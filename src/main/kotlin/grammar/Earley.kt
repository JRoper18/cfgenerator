package grammar

import java.io.PrintStream
import java.util.*


//Shamelessy stolen from here: https://github.com/tomerfiliba/tau/blob/master/Earley.java

/*
* Terminology
* ===========
* Consider the following context-free rule:
*
*     X -> A B C | A hello
*
* We say rule 'X' has two __productions__: "A B C" and "A hello".
* Each production is made of __production terms__, which can be either
* __terminals__ (in our case, "hello") or __rules__ (non-terminals, such as "A", "B", and "C")
*
*/
object Earley {
    // Epsilon transition: an empty production
    val Epsilon: Production = Production()

    @Throws(java.lang.Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        ///////////////////////////////////////////////////////////////////////////////////////
        // Simple mathematics expressions
        ///////////////////////////////////////////////////////////////////////////////////////
        val SYM = Rule("SYM", Production("a"))
        val OP = Rule("OP", Production("+"))
        val EXPR = Rule("EXPR", Production(SYM))
        EXPR.add(Production(EXPR, OP, EXPR))

        // note that this yields the catalan numbers sequence (as expected) -- the number of ways
        // to place parenthesis in the expression a + a + ... + a
        // this serves as a "semi-proof of correctness" :)
        println("catalan numbers:")
        for (text in arrayOf(
            "a", "a + a", "a + a + a", "a + a + a + a", "a + a + a + a + a",
            "a + a + a + a + a + a", "a + a + a + a + a + a + a"
        )) {
            val p = Parser(EXPR, text)
            System.out.printf("%d, ", p.trees.size)
        }
        println("\n")

        ///////////////////////////////////////////////////////////////////////////////////////
        // Simple rules for English
        ///////////////////////////////////////////////////////////////////////////////////////
        val N = Rule(
            "N", Production("time"), Production("flight"), Production("banana"),
            Production("flies"), Production("boy"), Production("telescope")
        )
        val D = Rule("D", Production("the"), Production("a"), Production("an"))
        val V = Rule(
            "V", Production("book"), Production("ate"), Production("sleep"),
            Production("saw"), Production("thinks")
        )
        val P = Rule(
            "P", Production("with"), Production("in"), Production("on"),
            Production("at"), Production("through")
        )
        val C = Rule("C", Production("that"))
        val PP = Rule("PP")
        val NP = Rule(
            "NP", Production(D, N), Production("john"), Production("bill"),
            Production("houston")
        )
        NP.add(Production(NP, PP))
        PP.add(Production(P, NP))
        val VP = Rule("VP", Production(V, NP))
        VP.add(Production(VP, PP))
        val S = Rule("S", Production(NP, VP), Production(VP))
        val Sbar = Rule("S'", Production(C, S))
        VP.add(Production(V, Sbar))

        // let's parse some sentences!
        for (text in arrayOf(
            "john ate a banana", "book the flight through houston",
            "john saw the boy with the telescope", "john thinks that bill ate a banana"
        )) {
            val p = Parser(S, text)
            System.out.printf("Parse trees for '%s'\n", text)
            println("===================================================")
            for (tree in p.trees) {
                tree.print(System.out)
                println()
            }
        }

        // let's fail
        var failed = false
        try {
            val p = Parser(S, "john ate")
        } catch (ex: ParsingFailed) {
            // okay
            failed = true
            println("hurrah, this has failed (we don't allow VP without a complement)")
        }
        if (!failed) {
            println("oops, this should have failed!")
        }
    }
    /*
	 * an abstract notion of the elements that can be placed within production
	 */
    interface ProductionTerm

    /*
    * Represents a terminal element in a production
    */
    class Terminal(val value: String) : ProductionTerm {
        override fun toString(): String {
            return value
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null) {
                return false
            }
            if (other is String) {
                return value == other
            } else if (other is Terminal) {
                return value == other.value
            }
            return false
        }
    }

    /*
	 * Represents a production of the rule.
	 */
    class Production : Iterable<ProductionTerm?> {
        val terms: MutableList<ProductionTerm>
        val rules: List<Rule>

        constructor(vararg terms: Any) {
            this.terms = ArrayList(terms.size)
            for (item in terms) {
                if (item is String) {
                    this.terms.add(Terminal(item))
                } else if (item is ProductionTerm) {
                    this.terms.add(item)
                } else {
                    throw IllegalArgumentException("Term must be ProductionTerm or String, not $item")
                }
            }
            rules = getRules()
        }

        fun size(): Int {
            return terms.size
        }

        operator fun get(index: Int): ProductionTerm {
            return terms[index]
        }

        override fun iterator(): Iterator<ProductionTerm> {
            return terms.iterator()
        }

        @JvmName("getRules1")
        private fun getRules(): List<Rule> {
            val rules = ArrayList<Rule>()
            for (term in terms) {
                if (term is Rule) {
                    rules.add(term)
                }
            }
            return rules
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            return if (other == null || other.javaClass != javaClass) {
                false
            } else (terms == (other as Production).terms)
        }

        override fun toString(): String {
            var s = ""
            if (!terms.isEmpty()) {
                for (i in 0 until terms.size - 1) {
                    val t = terms[i]
                    if (t is Rule) {
                        s += t.name
                    } else {
                        s += t
                    }
                    s += " "
                }
                val t = terms[terms.size - 1]
                if (t is Rule) {
                    s += t.name
                } else {
                    s += t
                }
            }
            return s
        }
    }

    /*
	 * A CFG rule. Since CFG rules can be self-referential, more productions may be added
	 * to them after construction. For example:
	 *
	 * Grammar:
	 * 	   SYM -> a
	 * 	   OP -> + | -
	 *     EXPR -> SYM | EXPR OP EXPR
	 *
	 * In Java:
	 *     Rule SYM = new Rule("SYM", new Production("a"));
	 *     Rule OP = new Rule("OP", new Production("+"), new Production("-"));
	 *     Rule EXPR = new Rule("EXPR", new Production(SYM));
	 *     EXPR.add(new Production(EXPR, OP, EXPR));            // needs to reference EXPR
	 *
	 */
    class Rule(val name: String, vararg productions: Production?) : ProductionTerm,
        Iterable<Production?> {
        val productions: ArrayList<Production>
        fun add(vararg productions: Production) {
            // No duplicates.
            val toAdd = mutableListOf<Production>()
            productions.forEach {
                if(!this.productions.contains(it)){
                    toAdd.add(it)
                }
            }
            this.productions.addAll(toAdd)
        }

        fun size(): Int {
            return productions.size
        }

        operator fun get(index: Int): Production {
            return productions[index]
        }

        override fun iterator(): Iterator<Production> {
            return productions.iterator()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || other.javaClass != javaClass) {
                return false
            }
            val other2 = other as Rule
            return name == other2.name && productions == other2.productions
        }

        override fun toString(): String {
            var s = name + " -> "
            if (!productions.isEmpty()) {
                for (i in 0 until productions.size - 1) {
                    s += productions[i].toString() + " | "
                }
                s += productions[productions.size - 1]
            }
            return s
        }

        init {
            this.productions = ArrayList(Arrays.asList(*productions))
        }
    }

    /*
	 * Represents a state in the Earley parsing table. A state has a its rule's name,
	 * the rule's production, dot-location, and starting- and ending-column in the parsing
	 * table.
	 */
    class TableState(val name: String, val production: Production, val dotIndex: Int, val startCol: TableColumn?) {
        var endCol: TableColumn? = null
        val isCompleted: Boolean
            get() = dotIndex >= production.size()
        val nextTerm: ProductionTerm?
            get() = if (isCompleted) {
                null
            } else production.get(dotIndex)

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }
            if (other == null || other.javaClass != javaClass) {
                return false
            }
            val other2 = other as TableState
            return name == other2.name && production == other2.production && dotIndex == other2.dotIndex && startCol === other2.startCol
        }

        override fun toString(): String {
            var s: String? = ""
            for (i in 0 until production.size()) {
                if (i == dotIndex) {
                    s += "\u00B7 "
                }
                val t = production[i]
                if (t is Rule) {
                    s += t.name
                } else {
                    s += t
                }
                s += " "
            }
            if (dotIndex == production.size()) {
                s += "\u00B7"
            }
            return String.format("%-6s -> %-20s [%d-%d]", name, s, startCol!!.index, endCol!!.index)
        }
    }

    /*
	 * Represents a column in the Earley parsing table
	 */
    class TableColumn(val index: Int, val token: String) : Iterable<TableState?> {
        val states: ArrayList<TableState>

        /*
		 * only insert a state if it is not already contained in the list of states. return the
		 * inserted state, or the pre-existing one.
		 */
        fun insert(state: TableState): TableState {
            val index = states.indexOf(state)
            return if (index < 0) {
                states.add(state)
                state.endCol = this
                state
            } else {
                states[index]
            }
        }

        fun size(): Int {
            return states.size
        }

        operator fun get(index: Int): TableState {
            return states[index]
        }

        /*
		 * since we may modify the list as we traverse it, the built-in list iterator is not
		 * suitable. this iterator wouldn't mind the list being changed.
		 */
        private inner class ModifiableIterator : MutableIterator<TableState> {
            private var i = 0
            override fun hasNext(): Boolean {
                return i < states.size
            }

            override fun next(): TableState {
                val st = states[i]
                i++
                return st
            }

            override fun remove() {}
        }

        override fun iterator(): Iterator<TableState> {
            return ModifiableIterator()
        }

        fun print(out: PrintStream, showUncompleted: Boolean) {
            out.printf("[%d] '%s'\n", index, token)
            out.println("=======================================")
            for (state in this) {
                if (!state.isCompleted && !showUncompleted) {
                    continue
                }
                out.println(state)
            }
            out.println()
        }

        init {
            states = ArrayList()
        }
    }

    /*
	 * A generic tree node
	 */
    class Node<T>(val value: T, private val children: List<Node<T>>) :
        Iterable<Node<T>> {
        override fun iterator(): Iterator<Node<T>> {
            return children.iterator()
        }

        fun print(out: PrintStream) {
            print(out, 0)
        }

        private fun print(out: PrintStream, level: Int) {
            var indentation = ""
            for (i in 0 until level) {
                indentation += "  "
            }
            out.println(indentation + value)
            for (child in children) {
                child.print(out, level + 1)
            }
        }
    }

    /*
	 * the exception raised by Parser should parsing fail
	 */
    class ParsingFailed(message: String?) : Exception(message) {
        companion object {
            private const val serialVersionUID = -3489519608069949690L
        }
    }

    /*
	 * The Earley Parser.
	 *
	 * Usage:
	 *
	 *     Parser p = new Parser(StartRule, "my space-delimited statement");
	 *     for (Node tree : p.getTrees()) {
	 *         tree.print(System.out);
	 *     }
	 *
	 */
    class Parser(startRule: Rule, text: String, val debug : Boolean  = false) {
        protected var columns: Array<TableColumn?>
        protected var finalState: TableState? = null

        /*
		 * the Earley algorithm's core: add gamma rule, fill up table, and check if the gamma rule
		 * spans from the first column to the last one. return the final gamma state, or null,
		 * if the parse failed.
		 */
        protected fun parse(startRule: Rule): TableState? {
            columns[0]!!.insert(TableState(GAMMA_RULE, Production(startRule), 0, columns[0]))
            for (i in columns.indices) {
                val col = columns[i]
                for (state in col!!) {
                    if (state.isCompleted) {
                        complete(col, state)
                    } else {
                        val term = state.nextTerm
                        if (term is Rule) {
                            predict(col, term)
                        } else if (i + 1 < columns.size) {
                            scan(columns[i + 1], state, (term as Terminal?)!!.value)
                        }
                    }
                }
                handleEpsilons(col)
                // DEBUG -- uncomment to print the table during parsing, column after column
                if(debug) {
                    col.print(System.out, false);
                }

            }

            // find end state (return null if not found)
            for (state in columns[columns.size - 1]!!) {
                if (state.name == GAMMA_RULE && state.isCompleted) {
                    return state
                }
            }
            return null
        }

        /*
		 * Earley scan
		 */
        private fun scan(col: TableColumn?, state: TableState, token: String) {
            if (token == col!!.token) {
                col.insert(TableState(state.name, state.production, state.dotIndex + 1, state.startCol))
            }
        }

        /*
		 * Earley predict. returns true if the table has been changed, false otherwise
		 */
        private fun predict(col: TableColumn?, rule: Rule): Boolean {
            var changed = false
            for (prod in rule) {
                val st = TableState(rule.name, prod, 0, col)
                val st2 = col!!.insert(st)
                changed = changed || (st === st2)
            }
            return changed
        }

        /*
		 * Earley complete. returns true if the table has been changed, false otherwise
		 */
        private fun complete(col: TableColumn?, state: TableState): Boolean {
            var changed = false
            for (st in state.startCol!!) {
                val term = st.nextTerm
                if (term is Rule && term.name == state.name) {
                    val st1 = TableState(st.name, st.production, st.dotIndex + 1, st.startCol)
                    val st2 = col!!.insert(st1)
                    changed = changed || (st1 === st2)
                }
            }
            return changed
        }

        /*
		 * call predict() and complete() for as long as the table keeps changing (may only happen
		 * if we've got epsilon transitions)
		 */
        private fun handleEpsilons(col: TableColumn?) {
            var changed = true
            while (changed) {
                changed = false
                for (state in col!!) {
                    val term = state.nextTerm
                    if (term is Rule) {
                        changed = changed || predict(col, term)
                    }
                    if (state.isCompleted) {
                        changed = changed || complete(col, state)
                    }
                }
            }
        }

        /*
		 * return all parse trees (forest). the forest is simply a list of root nodes, each
		 * representing a possible parse tree. a node is contains a value and the node's children,
		 * and supports pretty-printing
		 */
        val trees: List<Node<TableState?>>
            get() = buildTrees(finalState)

        /*
		 * this is a bit "magical" -- i wrote the code that extracts a single parse tree,
		 * and with some help from a colleague (non-student) we managed to make it return all
		 * parse trees.
		 *
		 * how it works: suppose we're trying to match [X -> Y Z W]. we go from finish-to-start,
		 * e.g., first we'll try to match W in X.endCol. let this matching state be M1. next we'll
		 * try to match Z in M1.startCol. let this matching state be M2. and finally, we'll try to
		 * match Y in M2.startCol, which must also start at X.startCol. let this matching state be
		 * M3.
		 *
		 * if we matched M1, M2 and M3, then we've found a parsing for X:
		 *
		 * X ->
		 *     Y -> M3
		 *     Z -> M2
		 *     W -> M1
		 *
		 */
        private fun buildTrees(state: TableState?): List<Node<TableState?>> {
            return buildTreesHelper(
                ArrayList(), state,
                state!!.production.rules.size - 1, state.endCol
            )
        }

        private fun buildTreesHelper(
            children: List<Node<TableState?>>,
            state: TableState?, ruleIndex: Int, endCol: TableColumn?
        ): List<Node<TableState?>> {
            val outputs = ArrayList<Node<TableState?>>()
            var startCol: TableColumn? = null
            if (ruleIndex < 0) {
                // this is the base-case for the recursion (we matched the entire rule)
                outputs.add(Node(state, children))
                return outputs
            } else if (ruleIndex == 0) {
                // if this is the first rule
                startCol = state!!.startCol
            }
            val rule = state!!.production.rules[ruleIndex]
            for (st in endCol!!) {
                if (st === state) {
                    // this prevents an endless recursion: since the states are filled in order of
                    // completion, we know that X cannot depend on state Y that comes after it X
                    // in chronological order
                    break
                }
                if (!st.isCompleted || st.name != rule.name) {
                    // this state is out of the question -- either not completed or does not match
                    // the name
                    continue
                }
                if (startCol != null && st.startCol !== startCol) {
                    // if startCol isn't null, this state must span from startCol to endCol
                    continue
                }
                // okay, so `st` matches -- now we need to create a tree for every possible
                // sub-match
                for (subTree in buildTrees(st)) {
                    // in python: children2 = [subTree] + children
                    val children2 = ArrayList<Node<TableState?>>()
                    children2.add(subTree)
                    children2.addAll(children)
                    // now try all options
                    for (node in buildTreesHelper(children2, state, ruleIndex - 1, st.startCol)) {
                        outputs.add(node)
                    }
                }
            }
            return outputs
        }

        companion object {
            // this is the name of the special "gamma" rule added by the algorithm
            // (this is unicode for 'LATIN SMALL LETTER GAMMA')
            private const val GAMMA_RULE = "\u0263" // "\u0194"
        }

        /*
		 * constructor: takes a start rule and a statement (made of space-separated words).
		 * it initializes the table and invokes earley's algorithm
		 */
        init {
            val tokens = text.split(" ").toTypedArray()
            columns = arrayOfNulls(tokens.size + 1)
            columns[0] = TableColumn(0, "")
            for (i in 1..tokens.size) {
                columns[i] = TableColumn(i, tokens[i - 1])
            }
            finalState = parse(startRule)
            if (finalState == null) {
                throw ParsingFailed("your grammar does not accept the given text")
            }
        }
    }
}

