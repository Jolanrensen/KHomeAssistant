package nl.jolanrensen.kHomeAssistant

actual object PrintException {
    private const val ANSI_RED = "\u001B[31m"

    actual fun print(message: String, e: Exception) {
        println(ANSI_RED + message)
        println(ANSI_RED + e.stackTrace.joinToString("\n"))
    }
}