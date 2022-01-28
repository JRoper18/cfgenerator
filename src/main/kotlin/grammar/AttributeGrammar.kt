package grammar

import grammar.constraints.ConstraintGenerator
import grammars.common.rules.*
import org.antlr.v4.runtime.*
import org.antlr.v4.tool.Grammar
import utils.DiscreteDistribution
import utils.duplicates


class AttributeGrammar(val givenRules: List<AttributedProductionRule>,
                       val constraints : Map<ProductionRule, ConstraintGenerator>,
                       val start : Symbol,
                       val scopeCloserRules : Set<ProductionRule> = setOf(),
                       val givenRuleWeights : DiscreteDistribution<ProductionRule> = DiscreteDistribution(givenRules.map {
                            Pair(it.rule, 1.0 / givenRules.size)
                       }.toMap()),
                        //The remaining weight of unspecified rules will be uniformly distributed among them.
                       val globalAttributeRegexes : Set<Regex> = givenRules.flatMap {
                           // By default, variable attributes are global.
                           if(it is VariableAttributeRule){
                               listOf(it.attrKeyRegex)
                           } else if (it is CombinedKeyedAttributesRule) {
                               it.flatRules().filterIsInstance<VariableAttributeRule>().map {
                                   it.attrKeyRegex
                               }
                           } else {
                               listOf()
                           }
                       }.toSet()){
    // Maps LHS symbols to a list of possible RHS symbol lists.
    val expansions: Map<Symbol, List<AttributedProductionRule>> by lazy {
        this.rules.groupBy {
            it.rule.lhs
        }
    }

    val givenSymbols: List<Symbol> = givenRules.flatMap {
        it.rule.rhs + it.rule.lhs
    }.distinct()

    val stringsetRules : Map<StringsetSymbol, Map<String, APR>> = givenSymbols.filterIsInstance<StringsetSymbol>().map {
        Pair(it, makeStringsetRules(it).toMap())
    }.toMap()
    val givenRulesToGlobalRules : Map<AttributedProductionRule, AttributedProductionRule> = (givenRules + stringsetRules.values.flatMap {
        it.values
    }).distinct().map { apr ->
        Pair(apr, GlobalCombinedAttributeRule(this.globalAttributeRegexes, apr, apr.rule in scopeCloserRules))
    }.toMap()

    val rules : List<AttributedProductionRule> = givenRulesToGlobalRules.values.toList()

    val symbols : List<Symbol> = rules.flatMap {
        it.rule.rhs + it.rule.lhs
    }.distinct()

    val ruleWeights = DiscreteDistribution<AttributedProductionRule>(rules.map {
        Pair(it, givenRuleWeights.weights[it.rule]!!)
    }.toMap())

    init {
        // Validate the grammar. Every non-terminal symbol should have an expansion.
        symbols.forEach {
            require(it.terminal || expansions.containsKey(it)) {
                "Symbol ${it.name} needs expansion rules"
            }
        }
        // Validate that each constraint actually correlates with a rule.
        constraints.forEach {
            require(givenRules.map {
                it.rule
            }.contains(it.key)) {
                "Rule ${it.key} is not present in the grammar but has a constraint."
            }
        }

        val prs = rules.map {
            it.rule
        }
        val prDups = prs.duplicates()
        require(prDups.isEmpty()) {
            "Duplicate production rules in grammar: $prDups"
        }
    }

    fun getPossibleExpansions(lhs: Symbol): List<AttributedProductionRule>{
        return this.expansions.getOrDefault(lhs, listOf())
    }

    fun makeDistributionOverRules(rules : Collection<AttributedProductionRule>): DiscreteDistribution<AttributedProductionRule> {
        return ruleWeights.filter {
            it in rules
        }
    }

    fun nodeRuleFromGivenRule(apr : APR) : APR {
        return givenRulesToGlobalRules[apr]!!
    }

    fun satisfies(prog : GenericGrammarNode) {
        for(child in prog.rhs) {
            satisfies(child)
        }
        val currentConsGen = constraints[prog.productionRule.rule] ?: return
        val currentCons = currentConsGen.generate(prog.attributes())
        for(cons in currentCons) {
            check(cons.satisfies(prog.attributes())) {
                "${prog.attributes()}\n$cons"
            }
        }
    }

    fun encode(prog : GenericGrammarNode, attrRegex : Regex = Regex(".*?"), extraAttrs : Map<GenericGrammarNode, NodeAttributes> = mapOf()) : String {
        var progStr = prog.toString(printAttrs = true, printAPR = false, prettyLines = false, splitAttrs = false,
        onlyPrintAttrs = attrRegex, extraAttrs = extraAttrs)
        rules.forEachIndexed { index, apr ->
            progStr = progStr.replace(apr.rule.toString(), index.toString())
        }
        progStr = progStr.replace("ATTRS: ", "")
        progStr = progStr.replace("->", "")
        return progStr
    }

    fun decode(str : String) : List<RootGrammarNode> {
        val lines = str.lines().filter {
            it.isNotBlank()
        }
        val res = decodeLines(lines, 0)
        check(lines.size == res.second) {
            "Only managed to decode up to line ${res.second}/${lines.size}"
        }
        return res.first
    }
    private fun decodeLines(lines : List<String>, depth : Int) : Pair<List<RootGrammarNode>, Int> {
        val nodes = mutableListOf<RootGrammarNode>()
        var currentNode : RootGrammarNode? = null
        var lineIdx = 0
        while(lineIdx < lines.size){
            val line = lines[lineIdx]
            for(i in 0 until depth) {
                if(line[i] != '\t') {
                    // This depth is over.
                    return Pair(nodes, lineIdx)
                }
            }
            val untabbedLine = line.substring(depth, line.length)
            if(untabbedLine[0] == '\t') {
                // Go down another level.
                val restOfStr = lines.subList(lineIdx, lines.size)
                val decodeRes = decodeLines(restOfStr, depth + 1)
                currentNode = currentNode!!.withChildren(decodeRes.first) as RootGrammarNode
                nodes.add(currentNode)
                lineIdx += decodeRes.second
            }
            else {
                // TODO: Find some way to escape this string when it appears naturally in a CFG
                val beforeAttrs = untabbedLine.substringBefore("{").trim()
                val aprIdx = beforeAttrs.toIntOrNull()
                val apr : AttributedProductionRule
                if(aprIdx == null) {
                    // Must not have a rule, which makes it a terminal.
                    val lhs = StringSymbol.fromPrintedString(beforeAttrs.trim())
                    apr = TerminalAPR(lhs)
                }
                else {
                    apr = rules[aprIdx]
                }
                currentNode = RootGrammarNode(apr)
                if(aprIdx == null) {
                    nodes.add(currentNode)
                    currentNode = null
                }
                lineIdx += 1
            }
        }
        return Pair(nodes.toList(), lineIdx)
    }

    /**
     * DEPRECATED
     */
    // Rulemap maps from antlr ruleNames to a list of APRs.
    data class AntlrResult(val grammarStr: String, val ruleMap: Map<String, List<APR>>)

    /**
     * DEPRECATED
     */
    fun toAntlr(name : String) : AntlrResult {
        val build = StringBuilder()
        build.append("grammar $name;\n")
        build.append("options {contextSuperClass=org.antlr.v4.runtime.RuleContextWithAltNum;}\n")
        // Include all symbols created in the stringset rules.
        val expandedSymbols = this.rules.flatMap {
            it.rule.rhs + it.rule.lhs
        }.filter {
            it.name.isNotEmpty() // Get rid of the terminal symbol (which is an empty string).
        }.toSet()
        val ruleMap = mutableMapOf<String, List<APR>>()
        var labelIdx = 0
        expandedSymbols.forEach { symbol ->
            val ruleBuilder = StringBuilder()
            val ruleName = symbol.toAntlrRuleName()
            if(symbol.terminal) {
                // Let's make a lexer rule from this.
                ruleBuilder.append("${symbol.toAntlrLexerName()} : '${symbol.toEscapedString()}'")
            } else {
                ruleBuilder.append("${ruleName} : ")
                var first = true
                val aprs = getPossibleExpansions(symbol)
                aprs.forEach { apr ->
                    if (!first) {
                        ruleBuilder.append("\n\t| ") // For every expansion, put it on a new line and indent it.
                    }
                    first = false
                    apr.rule.rhs.forEach { it ->
                        if (it.terminal) {
                            ruleBuilder.append("${it.toAntlrLexerName()} ") // This leads to the lexer rule
                        } else {
                            ruleBuilder.append("${it.toAntlrRuleName()} ") // This leads to the production rule.
                        }
                    }
//                    ruleBuilder.append("# Label$labelIdx")
                    labelIdx += 1

                }
                ruleMap[ruleName] = aprs
            }
            ruleBuilder.append("\n\t;")
            build.append(ruleBuilder)
            build.append("\n")
        }
        return AntlrResult(build.toString(), ruleMap.toMap())
    }

    /**
     * DEPRECATED
     */
    fun fromAntlrParsetree(grammar: Grammar, ctx: RuleContext, antlrToAPRS: Map<String, List<APR>>) : GenericGrammarNode {
        val rule = grammar.getRule(ctx.ruleIndex)
        val alts = antlrToAPRS[rule.name]!!
        val altIdx = if(ctx.altNumber == 0) 0 else ctx.altNumber-1
        val apr = alts[altIdx]
        val node = RootGrammarNode(apr)
        val childNodes = mutableListOf<GenericGrammarNode>()
        for(cidx in 0 until ctx.childCount) {
            val childData = ctx.getChild(cidx).payload
            when(childData) {
                is RuleContextWithAltNum -> {
                    childNodes.add(fromAntlrParsetree(grammar, childData, antlrToAPRS))
                }
                is CommonToken -> {
                    childNodes.add(RootGrammarNode(TerminalAPR(StringSymbol(childData.text))))
                }
            }
        }
        return node.withChildren(childNodes)
        // Docs: https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/ParseTree.html
    }
    // Until I get antlr working this is gone. 
    // fun parse(progStr: String, start : Symbol = this.start) : GenericGrammarNode {
    //     val tmpFile = File.createTempFile("tmp", ".g4")
    //     val antlrResult = toAntlr(tmpFile.name.substringBefore('.'))
    //     tmpFile.writeText(antlrResult.grammarStr)
    //     val g: Grammar = Grammar.load(tmpFile.path)
    //     val progStrByteStream = progStr.byteInputStream(StandardCharsets.UTF_8)
    //     val progStrStream = (CharStreams.fromStream(progStrByteStream, StandardCharsets.UTF_8));
    //     val lexEngine = g.createLexerInterpreter(progStrStream)
    //     val tokens = CommonTokenStream(lexEngine)
    //     val parser = DeepCoderParser(tokens)
    //     val ctx = parser.stmtList() as RuleContextWithAltNum
    //     println(ctx.toStringTree(parser))
    //     return fromAntlrParsetree(g, ctx, antlrResult.ruleMap)
    // }
}