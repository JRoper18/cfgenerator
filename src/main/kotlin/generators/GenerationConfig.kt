package generators

import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

        fun fromJson(json : String, ag : AttributeGrammar) : GenerationConfig {
            val sConfig = Gson().fromJson(json, SerializableGenerationConfig::class.java)
            return GenerationConfig(
                ruleWeights = DiscreteDistribution(ag.rules.zip(sConfig.orderedWeights).toMap()),
                numRandomTries = sConfig.numRandomTries,
                maxProgramDepth = sConfig.maxProgramDepth,
            )
        }

        val defaultMaxProgramDepth = 10
        val defaultNumRandomTries = 3
    }
    private data class SerializableGenerationConfig(val orderedWeights : List<Double>, val numRandomTries: Int, val maxProgramDepth: Int)
    fun toJson(ag : AttributeGrammar) : String {
        val sConfig = SerializableGenerationConfig(
            orderedWeights = ag.rules.map {
                ruleWeights.weights[it]!!
            },
            numRandomTries = numRandomTries,
            maxProgramDepth = maxProgramDepth
        )
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(sConfig)
        return jsonString
    }
}
