package nl.jolanrensen.kHomeAssistant

expect object PrintException {
    fun print(message: String = "", e: Exception)
}