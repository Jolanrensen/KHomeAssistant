package nl.jolanrensen.kHomeAssistant

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.launch
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.Switch
import nl.jolanrensen.kHomeAssistant.helper.Queue
import nl.jolanrensen.kHomeAssistant.messages.*
import nl.jolanrensen.kHomeAssistant.states.State
import nl.jolanrensen.kHomeAssistant.states.SwitchState

/**
 * KHomeAssistant instance
 *
 * run run() to make the instance run
 */
class KHomeAssistant(
        val host: String,
        val port: Int = 8123,
        val accessToken: String,
        val automations: HashSet<Automation> = hashSetOf(),
        val secure: Boolean = false,
        val debug: Boolean = false
) {

    /** HA version reported by the connected instance */
    lateinit var haVersion: String

    /** ID to represent the number of interactions with the HA instance */
    private var messageID: Int = 0

    /** println's only executed if debug=true */
    fun debugPrintln(message: Any?) {
        if (debug) println("DEBUG: $message")
    }

//    val stateListeners

    private val sendQueue = Queue<suspend DefaultClientWebSocketSession.() -> Unit>()

    private suspend fun sendMessage(message: suspend DefaultClientWebSocketSession.() -> Unit) {

    }

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    suspend fun run() {
        val block: suspend DefaultClientWebSocketSession.() -> Unit = {
            authenticate()
            initializeAutomations()

            // TODO get states
            val id = ++messageID
            send(
                    FetchStateMessage(id).toJson()
                            .also { debugPrintln(it) }
            )

            val response: FetchStateResponse = fromJson((incoming.receive() as Frame.Text).readText())

            println(response)


//            // receive and put in queue
//            launch {
//                while (true) {
//
//                }
//            }
//
//            // send from queue
//            while (true) {
//                delay(1)
//                if (!sendQueue.isEmpty()) {
//
//                }
//            }
            // TODO continue here
        }

        if (secure) httpClient.wss(
                host = host,
                port = port,
                path = "/api/websocket",
                block = block
        ) else httpClient.ws(
                host = host,
                port = port,
                path = "/api/websocket",
                block = block
        )
    }

    /** Authenticate  */
    private suspend fun DefaultClientWebSocketSession.authenticate() {
        var response = AuthResponse.fromJson(
                (incoming.receive() as Frame.Text).readText()
                        .also { debugPrintln(it) }
        )

        haVersion = response.ha_version

        if (response.isAuthRequired) {
            send(AuthMessage(access_token = accessToken).toJson())
            response = AuthResponse.fromJson(
                    (incoming.receive() as Frame.Text).readText()
                            .also { debugPrintln(it) }
            )
        }
        if (!response.isAuthOk) {
            throw IllegalArgumentException(response.message)
        }
        println("successfully logged in, connected to HASS instance of version $haVersion")
    }

    /** Initialize all automations asynchronously */
    private suspend fun DefaultClientWebSocketSession.initializeAutomations() {
        for (it in automations) launch {
            try {
                it.kHomeAssistant = this@KHomeAssistant
                it.initialize()
                println("Successfully initialized automation ${it.automationName}")
            } catch (e: Exception) {
                println("FAILED to initialize automation ${it.automationName}: $e")
            }
        }
    }

    suspend fun <S : State<*>, E : Entity<S>> getState(entity: E): S? {
        // TODO remove test
        return when (entity) {
            is Switch -> {

                sendQueue.enqueue {

                }


                object : SwitchState {
                    override val state: OnOff
                        get() = OnOff.ON
                } as S
            }
            else -> null
        }
    }


}