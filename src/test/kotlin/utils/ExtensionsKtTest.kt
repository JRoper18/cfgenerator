package utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ExtensionsKtTest {


    @Test
    fun testCombinationsLazy() {
        val c1 = listOf(1, 2, 3)
        assertEquals(c1.cartesian(c1).map {
            listOf(it.first, it.second)
        }, c1.combinationsLazy(2).toList())

        assertEquals(c1.combinations(3), c1.combinationsLazy(3).toList())
    }
}