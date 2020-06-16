package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.js.Promise

// Todo test
actual object RunBlocking {
    actual fun <T> runBlocking(
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): dynamic = GlobalScope.promise { block() }

}