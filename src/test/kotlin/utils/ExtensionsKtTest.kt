package utils

import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.Thread.sleep

internal class ExtensionsKtTest {


    @Test
    fun testCombinationsLazy() {
        val c1 = listOf(1, 2, 3)
        val combIter = c1.combinationsLazy(2)

        assertEquals(c1.cartesian(c1).map {
            listOf(it.first, it.second)
        }, combIter.asSequence().toList())

        assertEquals(c1.combinations(3), c1.combinationsLazy(3).asSequence().toList())
    }

    @Test
    fun testCombinationsDoesntFuckMyMemory() {
        val big = 100000
        val c1 = IntRange(0, big).toList()
        // If combinations is REALLY lazy this won't brick my computer.
        println("allocating...")
        (0 until big / 100).map {
            c1.combinationsLazy(big / 100)
        }.map {
            it.next()
        }
        println("allocated!")
    }
}