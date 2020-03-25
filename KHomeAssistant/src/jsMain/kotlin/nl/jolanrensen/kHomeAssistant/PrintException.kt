package nl.jolanrensen.kHomeAssistant

actual object PrintException {
    actual fun print(message: String, e: Exception) {
        console.error(message, e)
    }
}