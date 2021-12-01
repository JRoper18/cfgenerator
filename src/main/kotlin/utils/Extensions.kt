package utils

import java.util.stream.Stream

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

fun <T> List<T>.splitRecursive(downToken : T, upToken : T, seperatorToken : T) : List<List<T>> {
    val args = mutableListOf<List<T>>()
    var buildSubTokens = mutableListOf<T>()
    var depth = 0
    for(token in this) {
        if(depth != 0 || (depth == 0 && token != seperatorToken)) {
            buildSubTokens.add(token)
        }
        when(token){
            downToken -> depth += 1
            upToken -> depth -= 1
            seperatorToken -> {
                if(depth == 0) {
                    args.add(buildSubTokens)
                    buildSubTokens = mutableListOf()
                }
            }
        }
    }
    args.add(buildSubTokens)
    require(depth == 0) {
        "Non-recursive list $this"
    }
    return args
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

fun <T> Collection<T>.combinationsLazy(size : Int) : Stream<List<T>> {
    require(size >= 1) {
        "Size must be >= 1!"
    }
    if(size == 1) {
        return this.stream().map {
            listOf(it)
        }
    }
    else {
        return combinationsLazy(size - 1).flatMap { l ->
            this.stream().map { single ->
                l + single
            }
        }
    }

}
fun <T> Collection<T>.combinations(size : Int) : Collection<List<T>> {
    return combinationsLazy(size).toList()
}