package nl.jolanrensen.kHomeAssistant

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.serializer
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient
import nl.jolanrensen.kHomeAssistant.attributes.Attributes
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.SwitchEntity
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
        val secure: Boolean = false,
        val debug: Boolean = false,
        val automations: Collection<Automation>
) : KHomeAssistantContext {

//    constructor(
//            host: String,
//            port: Int = 8123,
//            accessToken: String,
//            secure: Boolean = false,
//            debug: Boolean = false,
//            automations: Collection<Automation>
//    ) : this(
//            host = host,
//            port = port,
//            accessToken = accessToken,
//            secure = secure,
//            debug = debug,
//            automations = automations.toMutableList()
//    )

//    constructor(
//            host: String,
//            port: Int = 8123,
//            accessToken: String,
//            secure: Boolean = false,
//            debug: Boolean = false,
//            vararg automations: Automation
//    ) : this(
//            host = host,
//            port = port,
//            accessToken = accessToken,
//            secure = secure,
//            debug = debug,
//            automations = automations.toMutableList()
//    )

    constructor(
            host: String,
            port: Int = 8123,
            accessToken: String,
            secure: Boolean = false,
            debug: Boolean = false,
            automationName: String = "Single Automation",
            automation: suspend Automation.() ->  Unit
    ) : this(
            host = host,
            port = port,
            accessToken = accessToken,
            secure = secure,
            debug = debug,
            automations = listOf(automation(automationName, automation))
    )


    override val kHomeAssistant = { this }

    lateinit var coroutineScope: CoroutineScope

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

    private val responseAwaiters = hashMapOf<Int, (String) -> Unit>()

    @OptIn(ImplicitReflectionSerializer::class)
    private suspend inline fun <reified Send: Message, reified Response: ResultMessage> sendMessage(message: Send): Response {
        val thisMessageID = ++messageID
        sendQueue.enqueue {
            message.id = thisMessageID
            val json = message.toJson()
            send(json)
            debugPrintln("Sent message: $json")
        }
        var response: Response? = null
        responseAwaiters[thisMessageID] = {
            debugPrintln("Received result response: $it")
            response = fromJson(it) // TODO fix
        }

        return withContext(coroutineScope.coroutineContext) {
            while (response == null) delay(1)
            response!!
        }
    }

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    suspend fun run() {
        val block: suspend DefaultClientWebSocketSession.() -> Unit = {
            coroutineScope = this
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
                    val message = incoming.poll() as? Frame.Text? ?: continue
                    val json = message.readText()
                    debugPrintln(json)

                    val messageBase: MessageBase = fromJson(json)
                    val id = messageBase.id
                    val type = messageBase.type

                    when (type) {
                        "result" -> responseAwaiters[id]?.invoke(json)?.run { responseAwaiters.remove(id) }
                        "event" -> {
                            val eventMessage: EventMessage = fromJson(json)
                            val event = eventMessage.event
                            debugPrintln("Detected event firing: $event")

                            when (event.event_type) {
                                "state_changed" -> {
                                    val eventDataStateChanged: EventDataStateChanged = fromJson(event.data.toString())
                                    val entityID = eventDataStateChanged.entity_id
                                    val newState = eventDataStateChanged.new_state

                                    // TODO update listeners for this entityID with this state change
                                    debugPrintln("Detected statechange $eventDataStateChanged")
                                }
                                "call_service" -> {
                                    val eventDataCallService: EventDataCallService = fromJson(event.data.toString())

                                    debugPrintln("Deteted call_service: $eventDataCallService")
                                    // TODO
                                }
                                // TODO maybe add more in the future
                            }

                        }
                    }

                }
            }

            val sender = launch {
                while (true) {
                    delay(1)
                    val message = sendQueue.dequeue() ?: continue
                    message()
                }
            }


            // TODO move, this is just for a test
            // registering for event messages:

            val res: ResultMessageBase = sendMessage(
                    SubscribeToEventMessage().also { debugPrintln(it) }
            )
            debugPrintln(res)


            receiver.join()
            sender.join()

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
        var response: AuthResponse = fromJson(
                (incoming.receive() as Frame.Text).readText()
                        .also { debugPrintln(it) }
        )

        haVersion = response.ha_version

        if (response.isAuthRequired) {
            send(AuthMessage(access_token = accessToken).toJson())
            response = fromJson(
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
                it.kHomeAssistant = { this@KHomeAssistant }
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
            is SwitchEntity -> {

                sendQueue.enqueue {

                }

                OnOff.ON as StateType
            }
            else -> null
        }
    }


}