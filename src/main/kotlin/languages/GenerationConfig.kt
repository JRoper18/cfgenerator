package languages

import grammar.AttributeGrammar
import grammar.AttributedProductionRule
import utils.DiscreteDistribution

data class GenerationConfig(
    val ruleWeights : DiscreteDistribution<AttributedProductionRule>,
    val numRandomTries : Int,
    val maxProgramDepth : Int,
) {
    constructor(ag : AttributeGrammar, numRandomTries: Int = defaultNumRandomTries, maxProgramDepth: Int = defaultMaxProgramDepth) : this(
        ruleWeights = weightsFromAG(ag), numRandomTries = numRandomTries, maxProgramDepth = maxProgramDepth
    )
    companion object {
        fun weightsFromAG(ag : AttributeGrammar): DiscreteDistribution<AttributedProductionRule> {
            return DiscreteDistribution<AttributedProductionRule>(
                ag.rules.map {
                    Pair(it, 1.0 / ag.rules.size.toDouble())
                }.toMap()
            )
        }

        val defaultMaxProgramDepth = 10
        val defaultNumRandomTries = 3
    }
}
