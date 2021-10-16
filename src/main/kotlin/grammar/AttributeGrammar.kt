package grammar

import grammar.constraints.ConstraintGenerator
import grammars.common.TerminalAPR
import grammars.common.makeStringsetRules
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import org.antlr.v4.tool.Grammar
import org.antlr.v4.tool.GrammarInterpreterRuleContext
import org.antlr.v4.tool.Rule
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.IllegalStateException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths


class AttributeGrammar(givenRules: List<AttributedProductionRule>, val constraints : Map<AttributedProductionRule, ConstraintGenerator>, val start : Symbol){
    // Maps LHS symbols to a list of possible RHS symbol lists.
    val expansions: Map<Symbol, List<AttributedProductionRule>> by lazy {
        this.rules.groupBy {
            it.rule.lhs
        }
    }

    val symbols: Collection<Symbol> = givenRules.flatMap {
        it.rule.rhs + it.rule.lhs
    }.toSet()

    val rules : Collection<AttributedProductionRule> = givenRules + symbols.flatMap { symbol ->
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
    }.toSet()

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

    // Rulemap maps from antlr ruleNames to a list of APRs.
    data class AntlrResult(val grammarStr: String, val ruleMap: Map<String, List<APR>>)

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