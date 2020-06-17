package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

actual object RunBlocking {
    actual fun <T> runBlocking(
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> T
    ) {
        kotlinx.coroutines.runBlocking(context, block)
    }
}