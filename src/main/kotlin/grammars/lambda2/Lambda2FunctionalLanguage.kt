package grammars.lambda2

import grammar.ProductionRule
import grammar.StringSymbol
import grammar.StringsetSymbol
import grammars.common.interpreters.*
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.COMMA
import grammars.common.rules.LP
import grammars.common.rules.OrderedListAttributeRule
import grammars.common.rules.RP
import utils.splitRecursive
import kotlin.random.Random

class Lambda2FunctionalLanguage() : TypedFunctionalLanguage(
    basicTypesToValues = mapOf(Lambda2.intType to IntRange(-1, 5).toSet(), Lambda2.boolType to setOf(true, false)),
    complexTypes = mapOf(Lambda2.listType to Lambda2.listTypeMapper),
    varName = Lambda2.varnames,
    typeAttr = Lambda2.typeAttr,
    functions = mapOf(
        "min" to MinFunction(Lambda2.intType, Lambda2.intListType),
        "max" to MaxFunction(Lambda2.intType, Lambda2.intListType),
        "indexinto" to IndexIntoFunction(Lambda2.listType, Lambda2.intType, Lambda2.listTypeMapper),
        "cons" to ConsFunction(Lambda2.listType, Lambda2.listTypeMapper),
        "concat" to ConcatFunction(Lambda2.listType),
        "len" to LengthFunction(Lambda2.listType, Lambda2.intType),
        "map" to MapFunction(Lambda2.lambdaType, Lambda2.listType, Lambda2.listTypeMapper),
        "filter" to FilterFunction(Lambda2.listType, Lambda2.boolType, Lambda2.lambdaType),
        "plus" to BinaryInt2IntExecutor(BinaryInt2IntExecutor.Operation.PLUS, Lambda2.intType),
        "minus" to BinaryInt2IntExecutor(BinaryInt2IntExecutor.Operation.MINUS, Lambda2.intType),
        "times" to BinaryInt2IntExecutor(BinaryInt2IntExecutor.Operation.TIMES, Lambda2.intType),
        "lt" to BinaryInt2BoolExecutor(BinaryInt2BoolExecutor.Operation.LT, Lambda2.intType, Lambda2.boolType),
        "gt" to BinaryInt2BoolExecutor(BinaryInt2BoolExecutor.Operation.GT, Lambda2.intType, Lambda2.boolType),
        "neg" to NegationExecutor(Lambda2.boolType),
        "equals" to EqualsExecutor(),
        "contains" to ContainsRule(Lambda2.listType, Lambda2.boolType),
        "or" to BinaryBool2BoolExecutor(BinaryBool2BoolExecutor.Operation.OR, Lambda2.boolType),
        "and" to BinaryBool2BoolExecutor(BinaryBool2BoolExecutor.Operation.AND, Lambda2.boolType),
    )
) {
    override fun makeFunctionPR(headerSymbol : StringSymbol, numArgs : Int, lambdaIdx : Int?) : ProductionRule {
        val pr = ProductionRule(stmtSym, listOf(headerSymbol, LP) + (0 until numArgs).flatMapIndexed { index, s ->
            val argSym = if(index == lambdaIdx) lambdaSym else stmtSym
            if(index != 0){
                listOf(COMMA, argSym)
            }
            else {
                listOf(argSym)
            }
        } + listOf(RP))
        return pr
    }

    override fun argIdxToChild(argIdx : Int) : Int {
        return (2 * argIdx) + 2
    }

    override fun ithChildTypeKey(argIdx : Int) : String {
        return "${argIdx}.${typeAttr}"
    }

    override fun ithLambdaArgTypeToKey(argIdx: Int) : String {
        return OrderedListAttributeRule.toAttrKey(argIdx, typeAttr)
    }

    override fun extractStmtData(tokens : List<String>) : Pair<String, List<List<String>>> {
        val funcName = tokens[0]
        if(tokens[1] != "(") {
            throw ParseError("Must open parenthesis $tokens")
        }
        if(tokens.last() != ")") {
            throw ParseError("Must close parenthesis $tokens")
        }
        val args = tokens.subList(2, tokens.size - 1).splitRecursive("(", ")", ",")
        return Pair(funcName, args)
    }

    override fun argsFromStr(args: String): List<Any> {
        val subArgs = args.toList().splitRecursive('[', ']', ',')
        return subArgs.map {
            argFromStr(it.joinToString("").trim())
        }
    }

    private fun argFromStr(arg : String) : Any {
        if(arg[0] == '[') {
            // Is it recursive?
            return argsFromStr(arg.substring(1, arg.length - 1))
        }
        // It's a constant.
        return (strsToConstants[arg] ?: throw ParseError("Unknown symbol $arg.trim()"))
    }

    override fun argsToStr(args: List<Any>): String {
        return args.map { arg ->
            when(arg){
                is List<*> -> arg.joinToString(separator = ",", prefix = "[", postfix = "]")
                else -> {
                    arg.toString()
                }
            }
        }.joinToString(",")
    }

    override fun lambdaVarnames(tokens : List<String>) : List<String> {
        val colonIdx = tokens.indexOf(":")
        val varnames = tokens.subList(1, colonIdx)
        return varnames.filter {
            it != ","
        }
    }
    override fun getStmtFromLambda(tokens : List<String>) : List<String> {
        return tokens.subList(tokens.indexOf(":") + 1, tokens.size)
    }
}