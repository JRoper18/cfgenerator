package utils

import kotlin.math.abs
import kotlin.random.Random

data class DiscreteDistribution<T>(val weights : Map<T, Double>){
    init {
        require(weights.isNotEmpty()) {
            "Must have some values in the input weights or some unweighted items!"
        }
        val wsum = weights.values.sum()
        require(abs(1.0 - wsum) < 0.00001) {
            "Weights must sum to 1 but instead sum to $wsum!"
        }
    }
    // Not sure if we need this.
    // val cumProbs = weights.toList().runningReduce { acc, current ->
    //     Pair(current.first, acc.second + current.second)
    // }
    fun sample(random : Random) : T {
        var cumProb = 0.0;
        val wanted = random.nextDouble(0.0, 1.0)
        val wList = weights.toList()
        for(weightPair in wList){
            cumProb += weightPair.second
            if(cumProb >= wanted){
                return weightPair.first
            }
        }
        return wList.last().first
    }

    fun sampledList(random : Random): List<T> {
        if(this.weights.size == 1) {
            return listOf(this.weights.keys.first())
        }
        // Pick an element,
        val ele = sample(random)
        // Sample from the rest
        val remainingToPick = this.filter {
            it != ele
        }
        val remainingList = remainingToPick.sampledList(random)
        return listOf(ele) + remainingList
    }

    fun filter(f : (T) -> Boolean): DiscreteDistribution<T> {
        val filteredWeights = weights.filter {
            f(it.key)
        }.toMap()
        return fromUnnormalized(filteredWeights)
    }
    companion object {
        fun <T> fromUnnormalized(inputWeights : Map<T, Double>): DiscreteDistribution<T> {
            val inputWeightTotal = inputWeights.values.sum()
            val weights = (inputWeights.map {
                Pair(it.key, it.value / inputWeightTotal)
            }).toMap()
            return DiscreteDistribution<T>(weights)
        }
    }
}
