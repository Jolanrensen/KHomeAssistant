import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import java.util.concurrent.Executors

val kHomeAssistant = KHomeAssistantInstance(
    host = TODO(),
    port = 8123,
    secure = true,
    debug = false,
    accessToken = TODO(),
//    coroutineDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher(),
    coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
)
