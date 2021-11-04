package grammars

import grammars.lambda2.Lambda2Interpreter

data class ProgramRunDetailedResult(val result: ProgramRunResult, val message : String) {
    companion object {
        fun fromInputOutput(input : String, actualOut : String, expectedOut : String) : ProgramRunDetailedResult {
            val rrs = ProgramRunResult.fromBool(actualOut == expectedOut)
            val msg = if(rrs.isGood()) "" else "\nActual Output: $actualOut"
            return ProgramRunDetailedResult(rrs, msg)
        }
    }
}