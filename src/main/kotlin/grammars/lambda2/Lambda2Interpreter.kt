package grammars.lambda2

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.random.Random
import kotlin.random.nextInt


class Lambda2Interpreter(val random : Random = Random(100L),
                         val maxFailedExamples : Int = 20,
                         val generatedIntRange : IntRange = IntRange(-20, 20),
                         val generatedListSizeRange : IntRange = IntRange(0, 4)
){
    companion object {
        val libStr = File("./src/main/kotlin/grammars/lambda2/lambda2.py").readText() + "\n"
    }
    internal class InterpretError(serr : String) : IllegalArgumentException(serr)
    internal enum class InputType {
        INTLISTLIST,
        INTLIST,
        INT,
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

    fun getNumberOfInputs(program: String) : Int {
        val script = libStr + "func = $program\nprint(func.__code__.co_argcount)"
        return runPyScript(script).toInt()
    }

    internal fun makeInput(inputType: InputType) : Any {
        // Yeah, yeah, I could make this recursive but it's... fine. For now.
        when(inputType) {
            InputType.INT -> return this.generatedIntRange.random(this.random)
            InputType.INTLIST -> return IntRange(0, this.generatedListSizeRange.random(this.random)).map {
                makeInput(InputType.INT)
            }
            InputType.INTLISTLIST -> return IntRange(0, this.generatedListSizeRange.random(this.random)).map {
                makeInput(InputType.INTLIST)
            }
        }
    }
    fun makeExamples(program : String, num : Int) : List<Pair<String, String>> {
        var numInputs = 0
        try {
            numInputs = getNumberOfInputs(program)
        } catch (ex: InterpretError) {
            return emptyList()
        }
        var inputTypes = Array<InputType?>(numInputs) {
            null
        }
        var numFails = 0
        val goodExamples = mutableListOf<Pair<String, String>>()
        for(i in 0 until num + maxFailedExamples) {
            val inputs = Array<Any>(numInputs) {
                it
            }
            val typesThisRun = inputTypes.copyOf()
            for(j in 0 until numInputs) {
                val inType = inputTypes[j] ?: InputType.values().random(this.random)
                inputs[j] = makeInput(inType)
                typesThisRun[j] = inType
            }
            var output : String? = null
            try {
                output = interp(program, args = inputs)
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
            inputTypes = typesThisRun
        }
        return goodExamples
    }

    private fun argsToStr(args: Array<Any>) : String {
        return args.joinToString(",")
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