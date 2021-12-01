package languages

import languages.lambda2.Lambda2FunctionalLanguage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TypedFunctionalLanguageTest {

    @Test
    fun testMakeNewVariableNode() {
        val varnames = listOf("a", "b", "c", "r", "q")
        varnames.forEach {
            Lambda2FunctionalLanguage().makeNewVariableNode(it).verify()
        }
    }

    @Test
    fun testMakeUnexpandedStmtNode() {
        val varnames = listOf("a", "b")
        val lan = Lambda2FunctionalLanguage()
        val prog = lan.makeLambdaWithStmt(varnames, "equals")
        println(lan.programToString(prog))
        prog.verify()
    }

    @Test
    fun testFillHoles() {
        val lan = Lambda2FunctionalLanguage(doSketch = true)
        val needsHolesProg = "lambda x : times ( x , 2 )"
        val wantedProg = "lambda x : times ( x , 3 )"
        // We're going to give it examples that are x times 3 and see if it can get that, instead.
        val examples = listOf(Pair("1", "3"), Pair("2", "6"))
        val sketched = lan.preprocessOnExamples(needsHolesProg, examples)
        assertNotEquals(sketched, needsHolesProg)
        for(example in examples) {
            assertTrue(lan.runProgramAgainstExample(sketched, example.first, example.second).result.isGood())
        }
        // There shouldn't be any such program that works with addition.
        val unfillableProg = "lambda x : plus ( x , 3 )"
        val unsketched = lan.preprocessOnExamples(unfillableProg, examples)
        assertEquals(unsketched, unfillableProg)
    }
}