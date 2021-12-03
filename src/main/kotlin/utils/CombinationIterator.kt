package utils

class CombinationIterator<T>(val l : Iterable<T>, val size : Int) : Iterator<List<T>>{
    class ListWrapIterator<T>(toWrap : Iterable<T>) : Iterator<List<T>> {
        private val iter = toWrap.iterator()
        override fun hasNext(): Boolean {
            return iter.hasNext()
        }

        override fun next(): List<T> {
            return listOf(iter.next())
        }

    }
    private var subIter : Iterator<List<T>>
    private val listWrapIter = ListWrapIterator(l)
    var currentSingleList : List<T>
    init {
        require(size > 1) {
            "Size must be > 1!"
        }
        subIter = makeSubIter()
        currentSingleList = listWrapIter.next()
    }
    private fun makeSubIter() : Iterator<List<T>> {
        if(size == 2) {
            return ListWrapIterator(l)
        } else {
            return CombinationIterator(l, size - 1)
        }
    }
    override fun hasNext(): Boolean {
        return listWrapIter.hasNext() || subIter.hasNext()
    }

    override fun next(): List<T> {

        if(!subIter.hasNext()) {
            subIter = makeSubIter()
            currentSingleList = listWrapIter.next()
        }
        val subL = subIter.next()
        return currentSingleList + subL
    }
}