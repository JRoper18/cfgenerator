package subscripts

import grammar.ProductionRule
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import languages.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import grammar.Symbol
import java.io.PrintWriter

data class LanguageMetadata(
    @SerializedName("symbols") val symbols : Array<Symbol>,
    @SerializedName("rules") val rules : Array<ProductionRule>
)

suspend fun <I, O> getMetadata(
    language : Language<I, O>,
    outputStream: PrintWriter = PrintWriter(System.out, true)
) {
    // We're going to put a json metadata of the language to stdout.
    val gson = GsonBuilder().setPrettyPrinting().create()
    // val gson = Gson()
    val g = language.grammar()
    val metadata = LanguageMetadata(
        symbols = g.symbols.toTypedArray(),
        rules = g.rules.map {it.rule}.toTypedArray()
    )
    val jsonStr = gson.toJson(metadata)
    outputStream.println(jsonStr)
    outputStream.flush()
}

suspend fun getMetadataCmd(args: Array<String>) {
    val parser = ArgParser("metadata")
    val lanChoice by parser.option(ArgType.Choice<LanguageRef>(), shortName = "l", description = "Input language to generate").required()
    val outputLoc by parser.option(ArgType.String, fullName = "output", shortName = "o", description = "Output file name")
    parser.parse(args)
    val lan = argsToLanguage(lanChoice)
    val outputStream : PrintWriter
    if(outputLoc == null) {
        outputStream = PrintWriter(System.out, true)
    } else {
        outputStream = PrintWriter(outputLoc)
    }
    getMetadata(lan, outputStream)

}