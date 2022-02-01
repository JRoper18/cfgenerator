package generators

import languages.lambda2.Lambda2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GenerationConfigTest {

    @Test
    fun testSerializable() {
        val ag = Lambda2.language.grammar
        val origCfg = GenerationConfig(ag = ag)
        val jsonStr = origCfg.toJson(ag)
        val newCfg = GenerationConfig.fromJson(jsonStr, ag)
        assertEquals(origCfg, newCfg)
    }
}