package languages

import generators.ProgramGenerator
import generators.ProgramStringifier
import grammar.*
import grammar.constraints.*
import grammars.common.mappers.SingleAttributeMapper
import grammars.common.rules.*
import interpreters.common.executors.FunctionExecutor
import interpreters.common.executors.HigherOrderFunctionExecutor
import interpreters.common.ProgramState
import interpreters.common.signatures.FunctionalPropertySignature
import interpreters.common.signatures.PropertySignature
import languages.sketchers.SimpleHoleAndSketcher
import languages.sketchers.VariableAndConstantSketcher
import utils.cartesian
import utils.combinationsTo
import utils.duplicates
import kotlin.random.Random
abstract class TypedFunctionalLanguage(
    val basicTypesToValues: Map<String, Set<Any>>,
    val complexTypes: Map<String, SingleAttributeMapper>,
    val varNameStringSet: StringsetSymbol,
    val functions: Map<String, FunctionExecutor>,
    val typeAttr: String,
    val properties: Set<FunctionalPropertySignature> = setOf(),
    val random: Random = Random,
    val maxComplexTypeDepth: Int = 2,
    val doSketch: Boolean = false
) : Language<List<Any>, Any> {

    val flattenedComplexTypes = complexTypes.entries.combinationsTo(maxComplexTypeDepth).cartesian(basicTypesToValues.keys).map {
        val startType = it.second
        var type = startType
        it.first.forEach { entry ->
            val mapper = entry.value
            type = mapper.forward(type)
        }
        val finalGenericType = it.first.last().key
        Pair(finalGenericType, type)
    }.groupBy {
        it.first
    }.map {
        Pair(it.key, it.value.map { it2 ->
            it2.second
        }.toSet())
    }.toMap()

    private val flattenedComplexToSpecificTypes = flattenedComplexTypes.flatMap { complexEntry ->
        val generic = complexEntry.key
        complexEntry.value.map { specific ->
            Pair(specific, generic)
        }
    }.toMap()

    fun genericTypeFromSpecific(specific : String) : String? {
        return flattenedComplexToSpecificTypes[specific]
    }
    val constantAttr : String = "constant"
    val lambdaType : String = "lambda"
    // Grammar stuff on the top
    val rootFunctionNameAttrKey = "rootStmtFunctionName"
    protected val stmtSym : NtSym = NtSym("stmt")
    protected val varInit = NtSym("varInit")
    protected val declaredVar = NtSym("declared")
    protected val newVarRule = VariableDeclarationRule(varInit, varNameStringSet, varNameStringSet.attributeName)
    protected val typedNewVarRule = VariableChildValueRule(
        varInit,
        varNameStringSet, newVarRule.subruleVarnameAttributeKey, "_typevar",
        typeAttr
    ).withOtherRule {
        SynthesizeAttributeProductionRule(mapOf(typeAttr to 0), it) // Bring up the ret type
    }
    protected val totalNewVarRule = newVarRule.withOtherRule(typedNewVarRule)
    protected val declaredRule = SynthesizeAttributeProductionRule(mapOf(varNameStringSet.attributeName to 0, typeAttr to 0), (PR(
        declaredVar, listOf(varNameStringSet))))

    protected val stmtIsDeclaredVarRule = SynthesizeAttributeProductionRule(
        mapOf(
            typeAttr to 0
        ), PR(stmtSym, listOf(declaredVar)))
    protected val lambdaArgs = NtSym("lambdaArgs")
    protected val lambdaArgsRule = OrderedListAttributeRule(ListProductionRule(listName = lambdaArgs, unitName = varInit, separator = ","),
        typeAttr, 3)
    protected val lambdaArgsInitRule = lambdaArgsRule.initListRule(0, PR(lambdaArgs, listOf(varInit)))
    protected val lambdaSym = NtSym("lambdaStmt")
    protected val lambdaRule = SynthesizeAttributeProductionRule(
        mapOf(typeAttr to 3) + lambdaArgsRule.attrKeysMade.map {
        Pair(it, 1) // Inherit every attribute from the lambdaArgs
    }.toMap(),
        PR(
            lambdaSym, listOf(
                StringSymbol(lambdaType),
                lambdaArgs, COLON,
                stmtSym
            )),
    )
    protected val functionNamesToRules = functions.map {
        val lambdaIdx : Int?
        if(it.value is HigherOrderFunctionExecutor) {
            lambdaIdx = 0
        }
        else {
            lambdaIdx = null
        }
        Pair(it.key, makeFunctionAPR(StringSymbol(it.key), it.value.numArgs, lambdaIdx) { pr ->
            it.value.makeReturnTypeAPR(this, pr)
        })
    }.toMap()

    protected val basicTypeConstantRules = basicTypesToValues.map {
        val sset = StringsetSymbol(it.value.map {
            it.toString()
        }.toSet(), attributeName = constantAttr)
        Pair(it.key, InitAttributeProductionRule(PR(stmtSym, listOf(sset)), typeAttr, it.key).withOtherRule { pr ->
            SynthesizeAttributeProductionRule(mapOf(sset.attributeName to 0), pr)
        })
    }.toMap()

    protected val strsToConstants = basicTypesToValues.values.flatten().map { constant ->
        Pair(constant.toString(), constant)
    }.toMap()

    protected val constantRules = basicTypeConstantRules.values.map {
        it.rule
    }.toSet()
    val grammar : AttributeGrammar = AttributeGrammar(
        givenRules = listOf(
            stmtIsDeclaredVarRule,
            totalNewVarRule,
            declaredRule,
            lambdaRule,
            lambdaArgsRule,
            lambdaArgsInitRule,
            ) + basicTypeConstantRules.values + functionNamesToRules.values,
        constraints = mapOf(
                declaredRule.rule to VariableConstraintGenerator(varNameStringSet.attributeName, newVarRule),
            ) + functions.map {
                Pair(functionNamesToRules[it.key]!!.rule, it.value.makeConstraints(
                    this,
                ))
            }.toMap(),
        start = lambdaSym,
        scopeCloserRules = setOf(
                lambdaRule.rule
        )
    )

    fun makeNewVariableNode(varname : String) : GenericGrammarNode {
        val varNameRules = grammar.stringsetRules[varNameStringSet]!!
        val apr = varNameRules[varname]!!
        return RootGrammarNode(grammar.nodeRuleFromGivenRule(totalNewVarRule)).withChildren(listOf(
            RootGrammarNode(grammar.nodeRuleFromGivenRule(apr)).withChildren(listOf(
                RootGrammarNode(TerminalAPR(StringSymbol(varname)))
            ))
        ))
    }

    /**
     * Returns a lambda with the varnames as arguments and a list of unexpanded statement args.
     */
    fun makeLambdaWithStmt(varnames : List<String>, functionName : String) : RootGrammarNode {
        val newVarNodes = varnames.map {
            makeNewVariableNode(it)
        }
        val lambdaArgsNode = (lambdaArgsRule.rule as ListProductionRule).roll(newVarNodes,
            grammar.nodeRuleFromGivenRule(lambdaArgsInitRule), grammar.nodeRuleFromGivenRule(lambdaArgsRule))
        val stmtRule = functionNamesToRules[functionName]!!
        val stmtNode = RootGrammarNode(grammar.nodeRuleFromGivenRule(stmtRule))
        val nameCons = BasicRuleConstraint(NodeAttribute(rootFunctionNameAttrKey, functionName))
        val progNode = RootGrammarNode(grammar.nodeRuleFromGivenRule(lambdaRule)).withChildren(listOf(
            RootGrammarNode(TerminalAPR(StringSymbol(lambdaType))),
            lambdaArgsNode,
            RootGrammarNode(TerminalAPR(COLON)),
            stmtNode
        )) as RootGrammarNode
        generator.expandNode(progNode.rhs[3], listOf(nameCons), generationConfig = GenerationConfig(
            ag = this.grammar, numRandomTries = 5, maxProgramDepth = 5
        ))
        return progNode
    }

    abstract fun makeFunctionPR(headerSymbol : StringSymbol, numArgs : Int, lambdaIdx : Int? = null) : ProductionRule

    abstract fun argIdxToChild(argIdx : Int) : Int

    abstract fun ithChildTypeKey(argIdx : Int) : String

    abstract fun ithLambdaArgTypeToKey(argIdx: Int) : String

    private fun makeFunctionAPR(headerSymbol: StringSymbol, numArgs: Int, lambdaIdx : Int?, returnTypeAttrRuleMaker : (ProductionRule) -> (KeyedAttributesProductionRule)) : AttributedProductionRule{
        val pr = makeFunctionPR(headerSymbol = headerSymbol, numArgs = numArgs, lambdaIdx=lambdaIdx)
        val returnTypeAttrRule = returnTypeAttrRuleMaker(pr)
        require(typeAttr in returnTypeAttrRule.attrKeysMade) {
            "Return type rule should make an attribute with key $typeAttr but makes keys ${returnTypeAttrRule.attrKeysMade}"
        }
        // Map the 3rd entry to 0.type, 5th entry to 1.type, and so on.
        var apr : KeyedAttributesProductionRule = returnTypeAttrRule
        for (i in 0 until numArgs){
            val cidx = argIdxToChild(i)
            apr = apr.withOtherRule(KeyChangeAttributeRule(pr, typeAttr, cidx, ithChildTypeKey(i)))
        }
        apr = apr.withOtherRule(InitAttributeProductionRule(pr, rootFunctionNameAttrKey, headerSymbol.name))
        return apr
    }
    // Then, the interpret stuff.
    class ParseError(msg : String) : IllegalStateException(msg)
    {
        constructor(msg : String, node : GenericGrammarNode, state : ProgramState) : this("Node:\n$node\n with program state: \n$state\n has error: \n$msg\n")

        constructor(msg : String, tokens : List<String>, state : ProgramState) : this("At program: ${tokens.joinToString(" ")} and state \n$state\n parse error with msg \n$msg\n")
    }

    class TypeError(val wantedType : String) : IllegalArgumentException(
        "Wanted type $wantedType"
    )

    class InterpretError(msg : String) : IllegalStateException(msg)

    internal fun makeInput(type: String) : Any {
        if(type in basicTypesToValues.keys) {
            return basicTypesToValues[type]!!.random(this.random)
        }
        // Perhaps it's a complex type.
        for(cType in flattenedComplexTypes.keys) {
            val flattenedVal = flattenedComplexTypes[cType]!!
            if(type in flattenedVal) {
                if(cType == "list") {
                    // TODO avoid hardcoding this
                    val unwrappedType = complexTypes[cType]!!.backward(type)
                    require(unwrappedType.isNotEmpty()) {
                        "Unknown type $type!"
                    }
                    return List(IntRange(0, 5).random(this.random)) {
                        makeInput(unwrappedType[0])
                    }
                }
            }
        }
        throw IllegalArgumentException("Unknown type $type!")
    }
    // Returns a list of names and types of lambda arrunProgramguments
    private fun getLambdaData(node : GenericGrammarNode) : List<Pair<String, String>> {
        val lambdaArgsNode = node.rhs[1]
        val inputsNodeList : List<GenericGrammarNode>
        if(lambdaArgsNode.productionRule.rule  == lambdaArgsRule.rule) {
            inputsNodeList = (lambdaArgsNode.productionRule.rule as ListProductionRule).unroll(lambdaArgsNode).map {
                it.rhs[0]
            }
        }
        else {
            // It's a list initialization.
            inputsNodeList = listOf(lambdaArgsNode.rhs[0].rhs[0])
        }
        val inputTypeList = inputsNodeList.map {
            it.attributes().getStringAttribute(typeAttr)!!
        }
        val inputVarnames = inputsNodeList.map {
            it.attributes().getStringAttribute(varNameStringSet.attributeName)!!
        }
        return inputVarnames.zip(inputTypeList)
    }

    // The subprogram examples map is a list of each arg's exampleRunData, empty if it's a constant/atomic.
    data class ExampleRunData(val input : List<Any>, val output : Any, val state : ProgramState = ProgramState(), val subprogramExamples : List<List<ExampleRunData>> = listOf(), val isLambdaRun : Boolean = false)
    fun makeExamples(progNode : RootGrammarNode, num : Int) : List<ExampleRunData> {
        val lambdaData = getLambdaData(progNode)
        var numFails = 0
        val goodExamples = mutableListOf<ExampleRunData>()
        val maxFailedExamples = 5
        for(i in 0 until num + maxFailedExamples) {
            val inputs = List<Any>(lambdaData.size) {
                makeInput(lambdaData[it].second)
            }
            var totalOutput : ExampleRunData? = null
            try {
                totalOutput = interpFull(stringifier.stringify(progNode), args = argsToStr(inputs))
            } catch (ex: Exception)  {
                when(ex) {
                    is InterpretError, is ParseError, is TypeError -> {
                        // Crap. Try again.
                        numFails += 1
                        if(numFails > maxFailedExamples) {
                            return goodExamples
                        }
                        continue // Don't hit the next part, where we solidify types and keep going.
                    }
                    else -> throw ex
                }
            }
            // If we're here the input worked.
            goodExamples.add(totalOutput)
            if(goodExamples.size >= num){
                return goodExamples;
            }
        }
        return goodExamples
    }


    val stringifier = ProgramStringifier(" ")
    private fun areTokensLambda(tokens : List<String>) : Boolean {
        return tokens[0] == lambdaType
    }
    abstract fun lambdaVarnames(tokens : List<String>) : List<String>
    fun getStmtChildIdxFromLambda() : Int {
        return 3
    }
    abstract fun getStmtFromLambda(tokens : List<String>) : List<String>

    /**
     * Returns the function names and the spans for each of it's args.
     */
    abstract fun extractStmtData(tokens : List<String>) : Pair<String, List<List<String>>>

    abstract fun argsFromStr(args : String) : List<Any>
    abstract fun argsToStr(args : List<Any>) : String

    fun tokenize(progStr : String) : List<String> {
        return progStr.split(" ").map {
            it.trim()
        }.filter { it.isNotBlank() }
    }
    fun detokenize(tokens : List<String>) : String {
        return tokens.joinToString(" ")
    }
    fun interpFull(progStr : String, args : String) : ExampleRunData {
        val exampleRunData = interpTokens(tokenize(progStr), argsFromStr(args))
        return exampleRunData
    }
    fun interp(progStr : String, args : String) : Any {
        return interpFull(progStr, args).output
    }
    private fun interpTokens(tokens : List<String>, args : List<Any>, programState : ProgramState = ProgramState()) : ExampleRunData {
        // Our language has two super-simple types of expressions: Statements and lambdas.
        val output : Any
        if(areTokensLambda(tokens)) {
            val varnames = lambdaVarnames(tokens)
            if(varnames.size != args.size) {
                throw ParseError("Args $args don't match lambda $tokens")
            }
            val dupVarnames = varnames.duplicates()
            if(dupVarnames.isNotEmpty()) {
                throw ParseError("Duplicate varnames $dupVarnames", tokens, programState)
            }
            varnames.forEachIndexed { index, name ->
                // Type is any if we don't know.
                programState.setVar(name, "any", args[index])
            }
            val internalStmt = getStmtFromLambda(tokens)
            val stmtResult = interpTokens(internalStmt, listOf(), programState = programState)
            varnames.forEach {
                programState.unsetVar(it)
            }
            return ExampleRunData(input = args, output = stmtResult.output, state = programState, subprogramExamples = listOf(listOf(stmtResult)), isLambdaRun = true)
        }
        else {
            // It's a statement. Either a variable, a constant, or a function call.
            if(tokens.size == 1) {
                // Variable, or constant?
                output = strsToConstants[tokens[0]] ?: programState.getVar(tokens[0]) ?: throw ParseError("Unknown token ${tokens[0]}")
                return ExampleRunData(args, output, programState, subprogramExamples = listOf(), isLambdaRun = false)
            }
            else {
                // Else, it's a function call.
                val stmtData = extractStmtData(tokens)
                val funcName = stmtData.first
                val executor = functions[funcName] ?: throw ParseError("Unknown function name $funcName")
                val lambdaIdx = 0
                // Lambda index at zero, just return the tokens unmodified.
                // Again, assuming lambdas are always the first arg.
                // This will totally bite me in the butt, but only if this actually works and I want to generalize to haskell.
                // Or higher-order functions that take more than one function as input.
                // If this approach doesn't work, then I won't have to worry about generalizing :/
                val subprogramExamples = MutableList<MutableList<ExampleRunData>>(stmtData.second.size) {
                    mutableListOf()
                }
                val interpedArgs = stmtData.second.mapIndexed { index, argTokens ->
                    if((index == lambdaIdx) && executor is HigherOrderFunctionExecutor) {
                        argTokens
                    }
                    else {
                        val runData = interpTokens(argTokens, listOf(), programState = programState)
                        // Add the run data to our current map of runs
                        subprogramExamples[index].add(runData)
                        runData.output
                    }
                }
                try {
                    output = executor.execute({ prog, theirArgs ->
                        val runResult = this.interpTokens(prog as List<String>, theirArgs, programState)
                        // Every time this is run, we want to record it.
                        subprogramExamples[lambdaIdx].add(runResult)
                        runResult.output
                    }, interpedArgs)
                } catch (ex : Exception) {
                    when(ex) {
                        is TypeError, is ParseError -> {
                            throw ex
                        }
                        else -> {
                            throw InterpretError(ex.stackTraceToString())
                        }
                    }
                }
                return ExampleRunData(args, output, programState, subprogramExamples = subprogramExamples, isLambdaRun = false)
            }
        }
    }


    // Finally, some language stuff.
    val generator = ProgramGenerator(this.grammar, random = this.random)
    override fun grammar(): AttributeGrammar {
        return grammar
    }

    override fun programToString(program: RootGrammarNode): String {
        return stringifier.stringify(program)
    }

    override fun runProgramAgainstExample(program: String, input: String, output: String): ProgramRunDetailedResult {
        try {
            val actual = runProgramWithExample(program, input).trim()
            return ProgramRunDetailedResult.fromInputOutput(input, actual, output.trim())
        } catch (iex : Exception) {
            val localMsg = iex.localizedMessage
            val rrs : ProgramRunResult;
            if(localMsg.contains("TypeError")) {
                rrs = ProgramRunResult.TYPEERROR
            }
            else if(localMsg.contains("NameError")) {
                rrs = ProgramRunResult.NAMEERROR
            }
            else {
                rrs = ProgramRunResult.RUNTIMEERROR
            }
            return ProgramRunDetailedResult(rrs, iex.localizedMessage)
        }
    }

    private fun exampleDataToNodePropertyMap(node : GenericGrammarNode, exs : Collection<ExampleRunData>) : Map<GenericGrammarNode, Map<FunctionalPropertySignature, PropertySignature.Result>> {
        if(exs.isEmpty()) {
            return mapOf()
        }
        val attrs = node.attributes()
        val thisOutputType = attrs.getStringAttribute(typeAttr)
        val theseInputTypes = mutableListOf<String>()
        var argIdx = 0;
        while(true) {
            val ithType = attrs.getStringAttribute(ithChildTypeKey(argIdx)) ?: break
            theseInputTypes.add(ithType)
            argIdx += 1
        }
        val rawExamples = exs.map {
            Pair(it.input, it.output)
        }
        val thisNodeSignatures = mutableMapOf<FunctionalPropertySignature, PropertySignature.Result>()
        this.properties.forEach {
            try {
                val sig = it.computeSignature(rawExamples)
                thisNodeSignatures[it] = sig
            } catch (ex : Exception) {
                // Not a good property.
                // TODO filter properties on types
            }
        }
        // Now calculate the children's property maps.
        val mergedChildrenPropMaps = exs.flatMap { example ->
            example.subprogramExamples.flatMapIndexed { argIdx, examples ->
                val subChild : GenericGrammarNode
                if(example.isLambdaRun) {
                    subChild = node.rhs[getStmtChildIdxFromLambda()]
                } else {
                    subChild = node.rhs[argIdxToChild(argIdx)]
                }
                val subPropMap = exampleDataToNodePropertyMap(subChild, examples)
                subPropMap.toList()
            }
        }.toMap()
        return (mergedChildrenPropMaps + mapOf(node to thisNodeSignatures)).toMap()
    }

    override fun generateProgramAndExamples(numExamples: Int, config: GenerationConfig): ProgramGenerationResult<List<Any>, Any> {
        val prog = generator.generate()
        val exampleDatas = makeExamples(prog, numExamples)
        val examples = exampleDatas.map {
            Pair(it.input, it.output)
        }
        val status = if(examples.isEmpty()) ProgramGenerationResult.PROGRAM_STATUS.BAD else ProgramGenerationResult.PROGRAM_STATUS.RUNNABLE
        val sigMap = exampleDataToNodePropertyMap(prog, exampleDatas)
        return ProgramGenerationResult(prog, examples, status, properties = sigMap)
    }

    override fun runProgramWithExample(program: String, input: String): String {
        return interp(program, input).toString()
    }

    override fun symbolsToAnalyse() : Set<String> {
        return this.functions.keys
    }

    override suspend fun preprocessOnExamples(program: String, examples: Collection<Pair<String, String>>) : String {
        if(!doSketch) {
            return program
        }
        val tokens = tokenize(program)
        val sketcher = VariableAndConstantSketcher(this, (this.strsToConstants.keys + varNameStringSet.stringset).toSet())
        val holes = sketcher.punchHoles(tokens).sorted().distinct()
        val fills = sketcher.makeFills(tokens, holes, examples) ?: return program
        val filled = sketcher.fill(tokens, holes, fills)
        return detokenize(filled)
    }
}
