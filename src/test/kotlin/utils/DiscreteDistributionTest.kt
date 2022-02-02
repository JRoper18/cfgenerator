package utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.Math.abs
import kotlin.random.Random

internal class DiscreteDistributionTest {

    val binary = DiscreteDistribution<String>(mapOf("a" to 0.5, "b" to 0.5))
    val list1 = "abcdefgh".toCharArray().toList().map { it.toString() }
    val d1 = DiscreteDistribution<String>(list1.map { Pair(it, 1.0 / list1.size) }.toMap())
    @Test
    fun testSample() {
        val r = Random
        // Sample as and bs. They should be close to each other.
        val samples = (0 until 500).map { binary.sample(r) }
        val aCount = samples.count { it == "a" }
        val bCount = samples.count { it == "b" }
        assertTrue(abs(aCount - bCount) < 50) {
            "Difference in counts: $aCount, $bCount"
        }
    }

    @Test
    fun testSampledList() {
        val r = Random
        val needed = list1.toSet()
        repeat(100) {
            assertEquals(needed, d1.sampledList(r).toSet())
        }
    }
}