package nl.jolanrensen.kHomeAssistant

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient
import nl.jolanrensen.kHomeAssistant.attributes.Attributes
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.Switch
import nl.jolanrensen.kHomeAssistant.helper.Queue
import nl.jolanrensen.kHomeAssistant.messages.*

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
) : WithKHomeAssistant {

    override val kHomeAssistant = this

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


//    private suspend fun <Send: Message, Response: Message> sendMessage(message: Message): Response {
//
//    }

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    suspend fun run() {
        val block: suspend DefaultClientWebSocketSession.() -> Unit = {
            authenticate()
            initializeAutomations()

            // TODO get states
//            val id = ++messageID
//            send(
//                    FetchStateMessage(id).toJson()
//                            .also { debugPrintln(it) }
//            )
//
//            val response: FetchStateResponse = fromJson((incoming.receive() as Frame.Text).readText())
//            val batik_json = response.result.first { it.entity_id == "light.batik" }
//
////            val batikState = LightState
//            val batikAttributes = LightAttributes.fromJson(batik_json.attributes.toString())
//
//            val batikState = Light("").parseStateValue(batik_json.state)
//
//            println(batik_json)
//            println(batikState)




            // receive and put in queue
            val receiver = launch {
                while (true) {
                    delay(1)
                    val message = incoming.poll() ?: continue

                    debugPrintln((message as Frame.Text).readText())
                }
            }


            send(
                    SubscribeToEventMessage(++messageID).toJson()
                            .also { debugPrintln(it) }
            )

            receiver.join()

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

    suspend fun <StateType : Any, AttributesType : Attributes, EntityType : Entity<StateType, AttributesType>>
            getAttributes(entity: EntityType): AttributesType? {


        return null
    }

    suspend fun <StateType : Any, AttributesType : Attributes, EntityType : Entity<StateType, AttributesType>>
            getState(entity: EntityType): StateType? {


        // TODO remove test
        return when (entity) {
            is Switch -> {

                sendQueue.enqueue {

                }

                OnOff.ON as StateType
            }
            else -> null
        }
    }


}