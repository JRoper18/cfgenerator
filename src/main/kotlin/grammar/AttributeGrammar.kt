package grammar

import grammar.constraints.ConstraintGenerator
import grammars.common.TERMINAL
import grammars.common.TerminalAPR
import grammars.common.makeStringsetRules

class AttributeGrammar(givenRules: List<AttributedProductionRule>, val constraints : Map<AttributedProductionRule, ConstraintGenerator>, val start : Symbol){
    // Maps LHS symbols to a list of possible RHS symbol lists.
    val expansions: Map<Symbol, List<AttributedProductionRule>> by lazy {
        this.rules.groupBy {
            it.rule.lhs
        }
    }

    val symbols: List<Symbol> = givenRules.flatMap {
        it.rule.rhs + it.rule.lhs
    }

    val rules : List<AttributedProductionRule> = givenRules + symbols.flatMap { symbol ->
        var ret = listOf<AttributedProductionRule>()
        when(symbol) {
            is StringsetSymbol -> {
                ret = makeStringsetRules(symbol)
            }
            is NonterminalSymbol -> {
                //Ignore
            }
            is StringSymbol -> {
                ret = listOf(TerminalAPR(symbol))
            }
        }
        ret
    }


    init {
        // Validate the grammar. Every non-terminal symbol should have an expansion.
        symbols.forEach {
            require(it.terminal || expansions.containsKey(it)) {
                "Symbol ${it.name} needs expansion rules"
            }
        }
        // Validate that each constraint actually correlates with a rule.
        constraints.forEach {
            require(givenRules.contains(it.key)) {
                "Rule ${it.key} is not present in the grammar but has a constraint."
            }
        }
    }

    fun getPossibleExpansions(lhs: Symbol): List<AttributedProductionRule>{
        return this.expansions.getOrDefault(lhs, listOf())
    }

    /*
    private fun makeEarleyFromExpansions(productionsToAPRS: MutableMap<Earley.Production, APR>, namesToRules : MutableMap<String, Earley.Rule>, lhs: Symbol) {
        if(lhs.terminal) {
            return
        }
        val nonRecursive = getPossibleExpansions(lhs).filter { apr ->
            !apr.rule.isRecursive()
        }
        val recursive = getPossibleExpansions(lhs).filter { apr ->
            apr.rule.isRecursive()
        }
        (nonRecursive + recursive).forEach{ apr ->
            val prodTerms = apr.rule.rhs.map{
                var ret : Earley.ProductionTerm
                if(expansions.containsKey(it) && !it.terminal) {
                    if(!namesToRules.containsKey(it.name)) {
                        makeEarleyFromExpansions(productionsToAPRS, namesToRules, it)
                    }
                    ret = namesToRules[it.name]!!
                }
                else {
                    ret = Earley.Terminal(it.name)
                }
                ret
            }
            val prod : Earley.Production
            if(prodTerms.isEmpty()) {
                prod = Earley.Production()
            }
            else {
                prod = Earley.Production(*prodTerms.toTypedArray())
            }
            if(!productionsToAPRS.containsKey(prod)) {
                // If we haven't already added/done something with this production yet:
                productionsToAPRS[prod] = apr
                if(namesToRules.containsKey(lhs.name)) {
                    namesToRules[lhs.name]!!.add(prod)
                }
                else {
                    val rule = Earley.Rule(lhs.name, prod)
                    namesToRules[lhs.name] = rule
                }

            }
        }
    }

    private fun makeTreeFromEarley(productionsToAPRS: MutableMap<Earley.Production, APR>, node: Earley.Node<Earley.TableState?>) : GenericGrammarNode {
        val children = node.map {
            makeTreeFromEarley(productionsToAPRS, it)
        }
        return RootGrammarNode(productionsToAPRS[node.value!!.production]!!).withChildren(children)
    }
    fun parse(str : String, fromSymbol : Symbol = this.start) : GenericGrammarNode {
        // First, tunr this into the custom format that Earley's requires.
        val namesToRules = mutableMapOf<String, Earley.Rule>()
        val productionsToAPRS = mutableMapOf<Earley.Production, APR>()
        symbols.forEach { sym ->
            makeEarleyFromExpansions(productionsToAPRS, namesToRules, sym)
        }
        for(rule in namesToRules.values) {
            rule.productions.forEach {
            }
        }
        val parser = Earley.Parser(namesToRules[fromSymbol.name]!!, str, debug=false)
        val trees = parser.trees
        // Just pick one.
        println(trees)
        val tree = trees[0]
        // Convert the tree to our custom format.
        val ggn = (makeTreeFromEarley(productionsToAPRS, tree))
        return ggn
    }
    */
    fun toAntlr(name : String) : String {
        val build = StringBuilder()
        build.append("grammar $name;\n")
        symbols.forEach { symbol ->
            val ruleBuilder = StringBuilder()
            if(symbol.terminal) {
                // Let's make a lexer rule from this.
                ruleBuilder.append("${symbol.toAntlrLexerName()} : '${symbol.toRegexEscapedString()}'")
            } else {
                ruleBuilder.append("${symbol.toAntlrRuleName()} : ")
                var first = true
                getPossibleExpansions(symbol).forEach { apr ->
                    if (!first) {
                        ruleBuilder.append("\n\t| ")
                    }
                    first = false
                    apr.rule.rhs.forEach {
                        if (it.terminal) {
                            ruleBuilder.append("${it.toAntlrLexerName()} ")
                        } else {
                            ruleBuilder.append("${it.toAntlrRuleName()} ")
                        }
                    }
                }
            }
            ruleBuilder.append("\n\t;")
            build.append(ruleBuilder)
            build.append("\n")
        }
        return build.toString()
    }
}