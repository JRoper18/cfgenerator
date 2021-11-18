package languages

enum class ProgramRunResult {
    SUCCESS,
    BAD,
    PARSEERROR,
    TYPEERROR,
    DECODEERROR,
    VERIFYERROR,
    NAMEERROR,
    RUNTIMEERROR;

    companion object {
        fun fromBool(inb : Boolean) : ProgramRunResult {
            return if(inb) SUCCESS else BAD
        }
    }

    fun isGood() : Boolean {
        return this == SUCCESS
    }

    fun isError() : Boolean {
        return this != SUCCESS && this != BAD
    }

    fun finishedRun() : Boolean {
        return !isError()
    }


}