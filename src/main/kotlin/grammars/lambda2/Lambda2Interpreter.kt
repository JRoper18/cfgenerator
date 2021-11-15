package grammars.lambda2

import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.RootGrammarNode
import grammars.common.rules.ListProductionRule
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.random.Random


class Lambda2Interpreter(val random : Random = Random(100L),
                         val maxFailedExamples : Int = 20,
                         val generatedIntRange : IntRange = IntRange(-20, 20),
                         val generatedListSizeRange : IntRange = IntRange(0, 4)
){
    companion object {
        val libStr = File("./src/main/kotlin/grammars/lambda2/lambda2.py").readText() + "\n"
    }
    internal class InterpretError(serr : String) : IllegalArgumentException(serr)
    fun programToString(prog : RootGrammarNode) : String {
        return ProgramStringifier(" ").stringify(prog)
    }
    internal fun runPyScript(script : String, ignoreErr : Boolean = false) : String {
        val output = StringBuilder()
        val rt = Runtime.getRuntime()
        val commands = arrayOf("python3", "-c", script)
        val proc = rt.exec(commands)

        val stdInput = BufferedReader(InputStreamReader(proc.inputStream))

        val stdError = BufferedReader(InputStreamReader(proc.errorStream))

        // Read the output from the command
        var s: String? = null
        while (stdInput.readLine().also { s = it } != null) {
            output.append(s)
        }
        // Read any errors from the attempted command
        val errStr = StringBuilder()
        while (stdError.readLine().also { s = it } != null) {
            errStr.append(s)
        }
        if(errStr.isNotEmpty() && !ignoreErr) {
            throw InterpretError(errStr.toString())
        }
        return output.toString()
    }

    fun hasSyntaxErr(program : String) : Boolean {
        val script = libStr + "\nhasSyntaxErr(\"func = \" + \"$program\")"
        val strout = runPyScript(script, true)
        return strout.toBooleanStrict()
    }

    internal fun makeInput(inputType: String) : Any {
        when(inputType) {
            "int" -> return this.generatedIntRange.random(this.random)
            "bool" -> return this.random.nextBoolean()
        }
        // Perhaps it's a list of ints or bools
        val unwrapped = Lambda2Grammar.listTypeMapper.backward(inputType)
        require(unwrapped.isNotEmpty()) {
            "Unknown type $inputType!"
        }
        return List(this.generatedListSizeRange.random(this.random)) {
            makeInput(unwrapped[0])
        }
    }
    fun makeExamples(progNode : RootGrammarNode, num : Int) : List<Pair<String, String>> {
        val progStr = programToString(progNode)
        val inputsNode = progNode.rhs[1]
        val inputsNodeList : List<GenericGrammarNode>
        if(inputsNode.productionRule.rule  == Lambda2Grammar.lambdaArgsRule.rule) {
            inputsNodeList = (inputsNode.productionRule.rule as ListProductionRule).unroll(inputsNode).map {
                it.rhs[0]
            }
        }
        else {
            // It's a list initialization.
            inputsNodeList = listOf(inputsNode.rhs[0].rhs[0])
        }
        val inputTypeList = inputsNodeList.map {
            it.attributes().getStringAttribute(Lambda2Grammar.retTypeAttrName)!!
        }
        val numInputs = inputsNode.attributes().getStringAttribute("length")!!.toInt()
        var numFails = 0
        val goodExamples = mutableListOf<Pair<String, String>>()
        for(i in 0 until num + maxFailedExamples) {
            val inputs = Array<Any>(numInputs) {
                makeInput(inputTypeList[it])
            }
            var output : String? = null
            try {
                output = interp(progStr, args = inputs)
            } catch (ex: InterpretError) {
                // Crap. Try again.
                numFails += 1
                if(numFails > maxFailedExamples) {
                    return goodExamples
                }
                continue // Don't hit the next part, where we solidify types and keep going.
            }
            // If we're here the input worked.
            goodExamples.add(Pair(argsToStr(inputs), output))
            if(goodExamples.size >= num){
                return goodExamples;
            }
        }
        return goodExamples
    }

    private fun argsToStr(args: Array<Any>) : String {
        return args.map {
            if(it is Array<*>) {
                "[" + argsToStr(it as Array<Any>) + "]"
            }
            else {
                it
            }
        }.joinToString(",")
    }

    fun interp(program: String, args2str: String) : String {
        val script = File("./src/main/kotlin/grammars/lambda2/lambda2.py").readText() + "\nfunc = $program\nprint(expand_iters(func($args2str)))"
        return runPyScript(script)
    }
    fun interp(program: String, args : Array<Any> = arrayOf()) : String {
        val args2str = argsToStr(args)
        return interp(program, args2str)
    }

}