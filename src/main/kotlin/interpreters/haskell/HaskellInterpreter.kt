package interpreters.haskell

import interpreters.simplepython.PythonInterpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.name

class HaskellInterpreter(
    val ghcLibDir : String = Path("/home/jroper18/.stack/programs/x86_64-linux/ghc-tinfo6-8.10.7/bin/").toString(),
    val helperLib : String = "HaskellHelper"
) {
    internal class InterpretError(val serr : String) : IllegalArgumentException(serr)

    fun astToScript(ast : String, ignoreErr : Boolean = false) : String {
        return runCommands(arrayOf(helperLib, "--mode", "pretty", "--stdin"), ast, ignoreErr = ignoreErr)
    }

    private fun runCommands(commands : Array<String>, inStr : String, ignoreErr : Boolean = false) : String {
        val output = StringBuilder()
        val rt = Runtime.getRuntime()
        val proc = rt.exec(commands)

        val stdInput = OutputStreamWriter(proc.outputStream)

        val stdOutput = BufferedReader(InputStreamReader(proc.inputStream))
        val stdError = BufferedReader(InputStreamReader(proc.errorStream))

        stdInput.write(inStr)
        stdInput.close()

        // Read the output from the command
        var s: String? = null
        while (stdOutput.readLine().also { s = it } != null) {
            output.append(s + "\n")
        }

        // Read any errors from the attempted command
        val errStr = StringBuilder()
        while (stdError.readLine().also { s = it } != null) {
            errStr.append(s + "\n")
        }
        if(errStr.isNotEmpty() && !ignoreErr) {
            throw InterpretError(errStr.toString())
        }
        return output.toString()
    }

    fun runHsScript(script : String, ignoreErr : Boolean = false) : String {
        return runCommands(arrayOf(Paths.get(ghcLibDir, "runhaskell").absolute().toString()), script, ignoreErr = ignoreErr)
    }
}