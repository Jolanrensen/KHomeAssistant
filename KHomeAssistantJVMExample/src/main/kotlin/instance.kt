import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import java.util.concurrent.Executors

val kHomeAssistant = KHomeAssistantInstance(
    host = "home.jolanrensen.nl",
    port = 8123,
    secure = true,
    debug = false,
    accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhNDgwZGMwN2FjNDA0N2JjYmZkY2ExOTQyZjViOWZjOSIsImlhdCI6MTU5NDc1NTAyMiwiZXhwIjoxOTEwMTE1MDIyfQ.LeVmxJ85DXrM5_3A1IRSgZSyaKwzEuZ_NxOsdk-CF-M",
//    coroutineDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher(),
    coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
)
