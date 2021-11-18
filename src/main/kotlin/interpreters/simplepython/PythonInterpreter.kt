package interpreters.simplepython

import generators.ProgramStringifier
import grammar.GenericGrammarNode
import grammar.RootGrammarNode
import grammars.common.rules.ListProductionRule
import grammars.lambda2.Lambda2Grammar
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.random.Random


class PythonInterpreter(
                        val typeAttr : String = "type",
                        val random : Random = Random(100L),
                        val maxFailedExamples : Int = 20,
                        val generatedIntRange : IntRange = IntRange(-20, 20),
                        val generatedListSizeRange : IntRange = IntRange(0, 4)
){
    companion object {
        val libStr = File("./src/main/kotlin/interpreters/simplepython/lib.py").readText() + "\n"
    }
    internal class InterpretError(serr : String) : IllegalArgumentException(serr)

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
        val script = File(libStr).readText() + "\nfunc = $program\nprint(expand_iters(func($args2str)))"
        return runPyScript(script)
    }
    fun interp(program: String, args : Array<Any> = arrayOf()) : String {
        val args2str = argsToStr(args)
        return interp(program, args2str)
    }

}