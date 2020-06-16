package nl.jolanrensen.kHomeAssistant

actual object PrintException {
    actual fun print(message: String, e: Throwable?) {
        console.error(message, e)
    }
}