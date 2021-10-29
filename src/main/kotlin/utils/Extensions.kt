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