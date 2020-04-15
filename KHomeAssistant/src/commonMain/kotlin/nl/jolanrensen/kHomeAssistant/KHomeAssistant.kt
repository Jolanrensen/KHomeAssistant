package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.DateTime
import com.soywiz.klock.until
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.Clock.cancelAllTimers
import nl.jolanrensen.kHomeAssistant.Clock.fixedRateTimer
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.messages.*
import kotlin.jvm.Volatile
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.minutes

/**
 * KHomeAssistant instance
 *
 * run run() to make the instance run
 */
@OptIn(ExperimentalTime::class)
class KHomeAssistant(
    val host: String,
    val port: Int = 8123,
    val accessToken: String,
    val secure: Boolean = false,
    val debug: Boolean = false,
//    val useCache: Boolean = true,
    val automations: Collection<Automation>
) : KHomeAssistantContext {

    constructor(
        host: String,
        port: Int = 8123,
        accessToken: String,
        secure: Boolean = false,
        debug: Boolean = false,
//        useCache: Boolean = true,
        automationName: String = "Single Automation",
        automation: suspend Automation.() -> Unit
    ) : this(
        host = host,
        port = port,
        accessToken = accessToken,
        secure = secure,
        debug = debug,
//        useCache = useCache,
        automations = listOf(automation(automationName, automation))
    )

    override val kHomeAssistant = { this }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PrintException.print("", throwable)
    }
    private val supervisor = SupervisorJob()
    override val coroutineContext = Dispatchers.Default + supervisor + exceptionHandler

    private val maxCacheAge = 15.minutes
    @Volatile
    private var cache: HashMap<String, StateResult> = hashMapOf()
    private var cacheAge = TimeSource.Monotonic.markNow()

    private suspend fun updateCache() {
        val response: FetchStateResponse = sendMessage(FetchStateMessage())
        response.result!!.forEach {
            cache[it.entity_id] = it
        }
        cacheAge = TimeSource.Monotonic.markNow()
        debugPrintln("Updated cache!")
    }


    /** HA version reported by the connected instance */
    lateinit var haVersion: String

    /** ID to represent the number of interactions with the HA instance */
    private var messageID: Int = 0
    private val messageIDMutex = Mutex()

    /** println's only executed if debug=true */
    fun debugPrintln(message: Any?) {
        if (debug) println("DEBUG: $message")
    }

    /** stateListeners["entity_id"] = set of listeners for this entity_id */
    val stateListeners: HashMap<String, HashSet<suspend (oldState: StateResult, newState: StateResult) -> Unit>> =
        hashMapOf()

    val scheduledRepeatedTasks: HashSet<RepeatedTask> = hashSetOf()

    private val sendQueue: Channel<suspend DefaultClientWebSocketSession.() -> Unit> = Channel(UNLIMITED)

    private val responseAwaiters: HashMap<Int, Channel<String>> = hashMapOf()

    @OptIn(ImplicitReflectionSerializer::class)
    private suspend inline fun <reified Send : Message, reified Response : ResultMessage> sendMessage(message: Send): Response {
        var thisMessageID: Int? = null
        messageIDMutex.withLock {
            thisMessageID = ++messageID

            sendQueue.send {
                message.id = thisMessageID!!
                val json = message.toJson()
                send(json)
                debugPrintln("Sent message: $json")
            }
        }

        // must wait for the results, or it's too fast for hass

        val receiveChannel = Channel<String>(1)
        responseAwaiters[thisMessageID!!] = receiveChannel
        val responseString = receiveChannel.receive()
        debugPrintln("Received result response: $responseString")

        return fromJson(responseString)
    }

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run() {
        println("KHomeAssistant started running at ${DateTime.nowLocal().utc}")
        val block: suspend DefaultClientWebSocketSession.() -> Unit = {
            val ioScope: CoroutineScope = this
            authenticate()

            // receive and put in queue
            val receiver = ioScope.launch {
                while (true) {
                    debugPrintln("receiver running!")
                    // receive or wait, break if connection closed
                    val message = incoming.receiveOrNull() as? Frame.Text ?: break
                    ioScope.launch {
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
                                        val oldState = eventDataStateChanged.old_state
                                        val newState = eventDataStateChanged.new_state

                                        cache[entityID]!!.apply {
                                            state = newState.state
                                            attributes = newState.attributes
                                        }

                                        stateListeners[entityID]?.forEach {
                                            this@KHomeAssistant.launch {
                                                try {
                                                    it(oldState, newState)
                                                } catch (e: Exception) {
                                                    PrintException.print("Error happened after state for entity $entityID changed from $oldState to $newState")
                                                    throw e
                                                }
                                            }
                                        }

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


            val sender = ioScope.launch {
                while (true) {
                    // receive from the sendQueue and send out
                    val message = sendQueue.receive()
                    message()
                    delay(1) // just to give hass some peace of mind
                }
            }


            fixedRateTimer(1000) {
                val now = DateTime.now()
                for (task in scheduledRepeatedTasks) {
                    while (task.alignWith < now.startOfSecond)
                        task.alignWith += task.runEvery

                    println("align with: ${task.alignWith.local}")
                    if (task.alignWith in now.startOfSecond until now.endOfSecond)
                        this@KHomeAssistant.launch { task.callback() }
                }
            }


//            var executionTime = 0.milliseconds
//            var previousTime = DateTime.now()
//            while (true) {
//                delay(1000 - executionTime.millisecondsLong)
//                executionTime = measureTime {
//                    val now = DateTime.now()
//                    val range = previousTime until now
//                    for (task in scheduledRepeatedTasks) {
//                        while (task.startingAt < previousTime) {
//                            task.startingAt += task.runEvery
//                        }
//                        if (task.startingAt in range) {
//                            this@KHomeAssistant.launch {
//                                task.callback()
//                            }
//                        }
//                    }
//                    previousTime = now
//                } + 0.8.milliseconds // Time correction
//
//            }
//            }

//            val scheduler = this@KHomeAssistant.launch {
//                var executionTime = 0.milliseconds
//                var previousTime = DateTime.now()
//                while (true) {
//                    delay(1000 - executionTime.millisecondsLong)
//                    executionTime = measureTime {
//                        val now = DateTime.now()
//                        val range = previousTime until now
//                        for (task in scheduledRepeatedTasks) {
//                            while (task.startingAt < previousTime) {
//                                task.startingAt += task.runEvery
//                            }
//                            if (task.startingAt in range) {
//                                this@KHomeAssistant.launch {
//                                    task.callback()
//                                }
//                            }
//                        }
//                        previousTime = now
//                    } + 0.8.milliseconds // Time correction
//
//                }
//            }

//            if (useCache)
            updateCache()
            registerToEventBus()

            initializeAutomations()

            // TODO maybe wait for the supervisor if possible to make sure all launches are done when running without listeners

            // cancel if there aren't any listeners and the automations are initialized
            println("All automations are initialized")
            if (stateListeners.isEmpty() && scheduledRepeatedTasks.isEmpty()) {
                println("There are no state listeners or scheduled tasks so KHomeAssistant is stopping...")
                cancelAllTimers()
                receiver.cancelAndJoin()
                sender.cancelAndJoin()
            } else {
                println("There are ${stateListeners.size} state listeners and ${scheduledRepeatedTasks.size} scheduled repeated tasks, so KHomeAssistant keeps running...")
                receiver.join()
                sender.join()
            }
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
        println("Successfully logged in, connected to HASS instance of version $haVersion")
    }

    /** Initialize all automations asynchronously */
    private suspend fun initializeAutomations() {
        val automationInitialisations = hashSetOf<Job>()
        for (it in automations)
            this@KHomeAssistant.launch {// TODO
                val inner = launch {
                    it.kHomeAssistantInstance = this@KHomeAssistant
                    it.initialize()
                }
                try {
                    inner.join()
                } catch (e: Exception) {
                    PrintException.print("FAILED to initialize automation \"${it.automationName}\"")
                    throw e
                }
                println("Successfully finished running initialize() of automation ${it.automationName}")
            }.also { automationInitialisations.add(it) }

        automationInitialisations.joinAll()
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
    suspend fun callService(
        entity: BaseEntity<*>,
        serviceDomain: Domain<*>,
        serviceName: String,
        data: Map<String, JsonElement> = mapOf()
    ) =
        callService(
            serviceDomain = serviceDomain.domainName,
            serviceName = serviceName,
            entityID = entity.entityID,
            data = data
        )


    /** Calls the given service */
    suspend fun callService(domain: Domain<*>, serviceName: String, data: Map<String, JsonElement> = mapOf()) =
        callService(
            serviceDomain = domain.domainName,
            serviceName = serviceName,
            data = data
        )

    /** Calls the given service */
    suspend fun callService(entity: BaseEntity<*>, serviceName: String, data: Map<String, JsonElement> = mapOf()) =
        callService(
            serviceDomain = entity.domain.domainName,
            entityID = entity.entityID,
            serviceName = serviceName,
            data = data
        )

    /** Calls the given service */
    suspend fun callService(
        serviceDomain: String,
        entityID: String? = null,
        serviceName: String,
        data: Map<String, JsonElement> = mapOf()
    ): ResultMessage =
        sendMessage<CallServiceMessage, ResultMessageBase>(
            CallServiceMessage(
                domain = serviceDomain,
                service = serviceName,
                service_data = JsonObject(
                    entityID?.let {
                        data + ("entity_id" to JsonPrimitive(it))
                    } ?: data
                )
            ).also { debugPrintln(it) }
        ).also { debugPrintln(it) }

    fun <StateType : Any, EntityType : BaseEntity<StateType>> getAttributes(
        entity: EntityType
    ): JsonObject {
        val attributesValue = try {
            if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }
            cache[entity.entityID]!!.attributes
        } catch (e: Exception) {
            throw Exception("The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.")
        }
        return attributesValue
    }

    fun <StateType : Any, EntityType : BaseEntity<StateType>> getState(
        entity: EntityType
    ): StateType {
        val stateValue = try {
//            if (useCache) {
            if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }
            cache[entity.entityID]!!.state
//            } else {
//                val response: FetchStateResponse = sendMessage(FetchStateMessage())
//                val entityJson = response.result!!.first { it.entity_id == entity.entityID }
//
//                debugPrintln("received entity's (${entity.name}) state: ${entityJson.state}")
//
//                entityJson.state
//            }
        } catch (e: Exception) {
            throw Exception("The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.")
        }

        return entity.parseStateValue(stateValue)!!
    }

//    suspend fun getMediaPlayerThumbnail(mediaPlayer: MediaPlayer.Entity): Array
}