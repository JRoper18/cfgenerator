package languages.sketchers

interface SimpleHoleAndSketcher {
    /**
     * Takes a program and returns a list of distinct indicies that should be removed ("punched" out as holes)
     */
    fun punchHoles(tokens : List<String>) : List<Int>

    /**
     * Returns a list of what each hole should be replaced with to fit the examples. Null if not found/not possible.
     */
    suspend fun makeFills(tokens : List<String>, holes : List<Int>, examples : Collection<Pair<String, String>>) : List<List<String>>?

    fun fill(tokens : List<String>, holes : List<Int>, fills : List<List<String>>) : List<String> {
        var holeIdx = 0
        require(fills.size == holes.size) {
            "Holes size ${holes.size} but given ${fills.size} fills!"
        }
        val newTokens = tokens.flatMapIndexed { index: Int, s: String ->
            if(holeIdx < holes.size && index == holes[holeIdx]) {
                val fill = fills[holeIdx]
                holeIdx += 1
                fill
            } else {
                listOf(s)
            }
        }
        return newTokens
    }
}