package utils

fun <T> List<T>.duplicates() : List<T> {
    val found = mutableSetOf<T>()
    val dups = mutableListOf<T>()
    for(ele in this){
        if(ele in found) {
            dups.add(ele)
        }
        else {
            found.add(ele)
        }
    }
    return dups.toList()
}

fun <T, U> Collection<T>.cartesian(other : Collection<U>) : Collection<Pair<T, U>> {
    return this.flatMap { t ->
        other.map { u ->
            Pair(t, u)
        }
    }
}
fun <T> Collection<T>.combinationsTo(size : Int) : Collection<List<T>> {
    return (1..size).flatMap {
        this.combinations(it)
    }
}
fun <T> Collection<T>.combinations(size : Int) : Collection<List<T>> {
    require(size >= 1) {
        "Size must be >= 1!"
    }
    if(size == 1) {
        return this.map {
            listOf(it)
        }
    }
    else {
        return combinations(size - 1).flatMap { l ->
            this.map { single ->
                l + single
            }
        }
    }
}