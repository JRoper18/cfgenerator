import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import subscripts.evaluateProgramsCmd
import subscripts.generateProgramsCmd
import kotlinx.coroutines.Dispatchers
import subscripts.getMetadataCmd


fun main(args: Array<String>) = runBlocking(Dispatchers.Default) {
    val runTypesToSubscripts = mapOf<String, (args: Array<String>) -> Unit>(
        "generate" to { this.launch {
                generateProgramsCmd(it)
            }
        },
        "evaluate" to {
            this.launch {
                evaluateProgramsCmd(it)
            }
        },
        "metadata" to {
            this.launch {
                getMetadataCmd(it)
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