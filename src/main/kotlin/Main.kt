import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import subscripts.evaluateDeepcoderPrograms
import subscripts.generateDeepcoderPrograms

fun main(args: Array<String>) = runBlocking {
    val runTypesToSubscripts = mapOf<String, (args: Array<String>) -> Unit>(
        "generate" to { this.launch {
                generateDeepcoderPrograms(it)
            }
        },
        "evaluate" to {
            this.launch {
                evaluateDeepcoderPrograms(it)
            }
        }
    )
    if(args.isEmpty()) {
        System.err.println("Must give a name of a subcommand to run! Possible subcommands: ")
        runTypesToSubscripts.keys.forEach {
            System.err.println(it)
        }
        return@runBlocking
    }
    val runType = args[0]
    val script = runTypesToSubscripts[runType]
    if(script == null){
        System.err.println("Cannot find subscript ${runType}")
        return@runBlocking
    }
    this.launch {
        script(args.copyOfRange(1, args.size))
    }
}