import subscripts.generatePrograms

fun main(args: Array<String>) {
    val runTypesToSubscripts = mapOf<String, (args: Array<String>) -> Unit>(
        "generate" to { generatePrograms(it) }
    )
    if(args.isEmpty()) {
        System.err.println("Must give a name of a subcommand to run! Possible subcommands: ")
        runTypesToSubscripts.keys.forEach {
            System.err.println(it)
        }
        return
    }
    val runType = args[0]
    val script = runTypesToSubscripts[runType]
    if(script == null){
        System.err.println("Cannot find subscript ${runType}")
        return
    }
    script(args.copyOfRange(1, args.size))
}