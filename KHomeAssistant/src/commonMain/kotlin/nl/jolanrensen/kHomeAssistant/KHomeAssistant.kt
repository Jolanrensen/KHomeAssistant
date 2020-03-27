package nl.jolanrensen.kHomeAssistant

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.attributes.attributesFromJson
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.EntityNotInHassException
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
        val justExecute: Boolean = false, // ignore all listeners, just execute the initialize of all automations TODO
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
            automation: suspend Automation.() -> Unit
    ) : this(
            host = host,
            port = port,
            accessToken = accessToken,
            secure = secure,
            debug = debug,
            automations = listOf(automation(automationName, automation))
    )


    override val kHomeAssistant = { this }

    var coroutineScope: CoroutineScope? = null

    /** HA version reported by the connected instance */
    lateinit var haVersion: String

    /** ID to represent the number of interactions with the HA instance */
    private var messageID: Int = 0

    /** println's only executed if debug=true */
    fun debugPrintln(message: Any?) {
        if (debug) println("DEBUG: $message")
    }

    /** stateListeners["entity_id"] = set of listeners for this entity_id */
    val stateListeners: HashMap<String, HashSet<suspend (StateResult) -> Unit>> = hashMapOf()

    private val sendQueue: Channel<suspend DefaultClientWebSocketSession.() -> Unit> = Channel(UNLIMITED)

    private val responseAwaiters: HashMap<Int, Channel<String>> = hashMapOf()

    @OptIn(ImplicitReflectionSerializer::class)
    private suspend inline fun <reified Send : Message, reified Response : ResultMessage> sendMessage(message: Send): Response {
        val thisMessageID = ++messageID
        sendQueue.send {
            message.id = thisMessageID
            val json = message.toJson()
            send(json)
            debugPrintln("Sent message: $json")
        }

        val receiveChannel = Channel<String>(1)

        responseAwaiters[thisMessageID] = receiveChannel

        val responseString = receiveChannel.receive()
        debugPrintln("Received result response: $responseString")

        return fromJson(responseString)
    }

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run() {
        val block: suspend DefaultClientWebSocketSession.() -> Unit = {
            coroutineScope = this
            authenticate()
            initializeAutomations()

            var receiver: Job? = null

            // receive and put in queue
            if (!justExecute) {
                receiver = launch {
                    while (true) {
                        debugPrintln("receiver running!")
                        // receive or wait, break if connection closed
                        val message = incoming.receiveOrNull() as? Frame.Text ?: break
                        launch {
                            val json = message.readText()
                            debugPrintln(json)

                            val messageBase: MessageBase = fromJson(json)
                            val id = messageBase.id
                            val type = messageBase.type

                            when (type) {
                                "result" -> responseAwaiters[id]?.send(json)?.run { responseAwaiters.remove(id) }
                                "event" -> {
                                    val eventMessage: EventMessage = fromJson(json)
                                    val event = eventMessage.event
                                    debugPrintln("Detected event firing: $event")

                                    when (event.event_type) {
                                        "state_changed" -> {
                                            val eventDataStateChanged: EventDataStateChanged = fromJson(event.data)
                                            val entityID = eventDataStateChanged.entity_id
                                            val newState = eventDataStateChanged.new_state

                                            stateListeners[entityID]?.forEach {
                                                launch { it(newState) }
                                            }

                                            // TODO update listeners for this entityID with this state change
                                            debugPrintln("Detected statechange $eventDataStateChanged")
                                        }
                                        "call_service" -> {
                                            val eventDataCallService: EventDataCallService = fromJson(event.data)

                                            debugPrintln("Deteted call_service: $eventDataCallService")
                                            // TODO
                                        }
                                        // TODO maybe add more in the future
                                    }
                                }
                            }
                        }
                    }
                    println("Receiver channel closed")
                }
            }

            val sender = launch {
                while (true) {
                    // receive from the sendQueue and send out
                    val message = sendQueue.receive() // TODO?: if (justExecute) break else continue
                    launch {
                        message()
                    }
                }
                println("Sent all messages in the queue to Home Assistant!")
            }

            if (!justExecute) registerToEventBus()

            receiver?.join()
            sender.join()
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
                PrintException.print("FAILED to initialize automation \"${it.automationName}\" because of: $e\n${e.message}\n${e.cause}", e)
            }
        }
    }


    /** Registering to Event bus */
    private suspend fun registerToEventBus() {
        val res: ResultMessageBase = sendMessage(
                SubscribeToEventMessage().also { debugPrintln(it) }
        )
        if (res.success)
            debugPrintln("Successfully registered to event bus!")
        else
            debugPrintln("Error registering to event bus: ${res.result}")
    }

    /** Calls the given service */
    suspend fun callService(domain: Domain<*>, serviceName: String, data: Map<String, JsonElement> = mapOf()) =
            callService(
                    domain = domain.domainName,
                    serviceName = serviceName,
                    data = data
            )

    /** Calls the given service */
    suspend fun callService(entity: BaseEntity<*, *>, serviceName: String, data: Map<String, JsonElement> = mapOf()) =
            callService(
                    domain = entity.domain.domainName,
                    entityName = entity.name,
                    serviceName = serviceName,
                    data = data
            )

    /** Calls the given service */
    suspend fun callService(domain: String, entityName: String? = null, serviceName: String, data: Map<String, JsonElement> = mapOf()): ResultMessage =
            sendMessage<CallServiceMessage, ResultMessageBase>(
                    CallServiceMessage(
                            domain = domain,
                            service = serviceName,
                            service_data = JsonObject(
                                    entityName?.let {
                                        data + ("entity_id" to JsonPrimitive("$domain.$it"))
                                    } ?: data
                            )
                    ).also { debugPrintln(it) }
            ).also { debugPrintln(it) }

    suspend fun <StateType : Any, AttributesType : BaseAttributes, EntityType : BaseEntity<StateType, AttributesType>> getAttributes(entity: EntityType, serializer: KSerializer<AttributesType>): AttributesType {
        val response: FetchStateResponse = sendMessage(FetchStateMessage())
        val entityJson = try {
            response.result!!.first { it.entity_id == entity.entityID }
        } catch (e: NoSuchElementException) {
            throw EntityNotInHassException("The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.")
        }
        debugPrintln("received entity's (${entity.name}) attributes: ${entityJson.attributes}")
        return attributesFromJson(entityJson.attributes, serializer)
    }

    suspend fun <StateType : Any, AttributesType : BaseAttributes, EntityType : BaseEntity<StateType, AttributesType>> getState(entity: EntityType): StateType {
        val response: FetchStateResponse = sendMessage(FetchStateMessage())
        val entityJson = try {
            response.result!!.first { it.entity_id == entity.entityID }
        } catch (e: NoSuchElementException) {
            throw EntityNotInHassException("The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.")
        }
        debugPrintln("received entity's (${entity.name}) state: ${entityJson.attributes}")
        return entity.parseStateValue(entityJson.state)!!
    }
}