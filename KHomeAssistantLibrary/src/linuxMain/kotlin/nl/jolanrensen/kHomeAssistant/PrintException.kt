package nl.jolanrensen.kHomeAssistant

actual object PrintException {
    private const val ANSI_RED = "\u001B[31m"
    private const val ANSI_WHITE = "\u001B[37m"

    actual fun print(message: String, e: Throwable?) {
        println(ANSI_RED + message)
        println(ANSI_RED + e?.message)
        println(ANSI_RED + e?.cause)
        println(ANSI_RED + e?.getStackTrace()?.joinToString("\n"))
        println(ANSI_WHITE)
    }
}