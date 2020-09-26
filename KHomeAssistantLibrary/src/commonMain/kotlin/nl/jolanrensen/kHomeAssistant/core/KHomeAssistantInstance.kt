package nl.jolanrensen.kHomeAssistant.core

import com.soywiz.klock.*
import com.soywiz.korim.bitmap.NativeImage
import com.soywiz.korim.format.decodeImageBytes
import com.soywiz.korio.async.delay
import com.soywiz.korio.util.encoding.Base64
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.EntityNotInHassException
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT
import nl.jolanrensen.kHomeAssistant.messages.*
import kotlin.jvm.Volatile
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.minutes
import io.ktor.client.features.websocket.wss
import io.ktor.client.features.websocket.ws

/**
 * KHomeAssistant instance.
 * This is used to connect to your Home Assistant server and start running the provided automations.
 * Run [run] from a suspended context (a suspend fun or in a `runBlocking { }` coroutine scope).
 *
 * @param host the address on which to reach your Home Assistant server. Like "https://myHomeAssistant.com"
 * @param port the port for your Home Assistant (API), usually 8123
 * @param accessToken the Long-lived Access Token that can be generated from your Home Assistant profile settings
 * @param secure whether to connect over SSL or not (https or http)
 * @param timeout timeout for confirmation for updating states and attributes
 * @param debug if enabled, debug messages will be printed
 */
@OptIn(ExperimentalTime::class)
class KHomeAssistantInstance(
    /** The address on which to reach your Home Assistant server. Like "https://myHomeAssistant.com". */
    val host: String,

    /** The port for your Home Assistant (API), usually 8123. */
    val port: Int = 8123,

    /** The Long-lived Access Token that can be generated from your Home Assistant profile settings. */
    val accessToken: String,

    /** Whether to connect over SSL or not (https or http). */
    val secure: Boolean = false,

    /** Timeout for confirmation for updating states and attributes. */
    val timeout: TimeSpan = 2.seconds,

    /** If enabled, debug messages will be printed. */
    override val debug: Boolean = false,

    /** If enabled, the connection will be acquired again on close. */
    val reconnectOnClose: Boolean = true
) : KHomeAssistant {

    override var loadedInitialStates: Boolean = false

    override val instance: KHomeAssistantInstance = this

    /** A collection of [Automation] instances that should be run by [KHomeAssistantInstance]. */
    private var automations: Collection<Automation> = listOf()

    /** All exceptions from couroutines in this scope will be handled here. */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PrintException.print("", throwable)
    }

    /** Makes sure that separate coroutines can fail independently. */
    private val supervisor = SupervisorJob()

    /** The context of the current coroutine scope */
    override val coroutineContext = Dispatchers.Default + supervisor + exceptionHandler

    /** The cache for the state and attributes of all entities in Home Assistant. */
    @Volatile
    private var rawEntityData: HashMap<String, StateResult> = hashMapOf()

    /** Returns all the raw entity IDs known in Home Assistant. */
    override val entityIds: Set<String>
        get() = rawEntityData.keys

    /** Returns all entities in Home Assistant as [Entity] (since we cannot know all possible types of entities) */
    override val entities: List<DefaultEntity>
        get() = entityIds.map {
            val (domain, name) = it.split(".")
            Domain(domain)[name]
        }

    /** The age of the [rawEntityData]. */
    private var cacheAge = TimeSource.Monotonic.markNow()

    /** The time after which to do a full refresh of the state/attributes of all entities.
     * Usually KHomeAssistant relies on state change events. */
    private val maxCacheAge = 15.minutes


    /** HA version reported by the connected instance */
    override lateinit var haVersion: String

    /** ID to represent the number of interactions with the HA instance. Used in [sendMessage]. */
    private var messageID: Int = 0

    /** Mutex that has to be used to modify [messageID] to prevent race conditions. */
    private val messageIDMutex = Mutex()

    /** All the event listeners, `eventListeners["event"] =` set of listeners for this event type. */
    override val eventListeners: HashMap<String, HashSet<suspend (Event) -> Unit>> = hashMapOf()

    /** All the state/attribute listeners. stateListeners["entity_id"] = set of listeners for this entity_id */
    override val stateListeners: HashMap<String, HashSet<StateListener>> = hashMapOf()

    /** The current receiver thread. Responsible for receiving all messages from Home Assistant. */
    private var receiver: Job? = null

    /** The current sender thread. Responsible for sending all messages to Home Assistant. */
    private var sender: Job? = null

    /** The queue containing all the messages to be sent to Home Assistant using [startSender]. */
    private val sendQueue: Channel<String> = Channel(UNLIMITED)

    /** A map containing channels waiting for a response with the given `messageID: Int`. */
    @Volatile
    private var responseAwaiters: HashMap<Int, Channel<String?>> = hashMapOf()

    private var connectionWasAlive = false


    /** The receiver channel has priority over the sending channel (Think updating state values, getting responses etc.).
     * That's why this boolean is used to make the sender wait until there is no receiving data from Home Assistant. */
    @Volatile
    private var canSend = true

    /** Channel to signal that [canSend] has been set to `true`. */
    private val canSendChannel = Channel<Unit>()

    /** Instance of the scheduler for this KHomeAssistant instance. */
    private val scheduler = Scheduler(this)

    /**
     * Allows to inline creation of [KHomeAssistantInstance] and automations and starting a run.
     *
     * Example:
     * ```
     * KHomeAssistantInstance(...).run(
     *      automation("some") {
     *          // do somehting
     *      },
     *      automation("other") {
     *          // do something
     *      }
     * )
     * ```
     * */
    suspend fun run(
        vararg functionalAutomations: FunctionalAutomation,
        mode: Mode = Mode.AUTOMATIC
    ) = run(automations = functionalAutomations.map { it.invoke(this) }.toTypedArray(), mode = mode)

    /**
     * Allows to inline creation of [KHomeAssistantInstance] and automations and starting a run.
     *
     * Example:
     * ```
     * KHomeAssistantInstance(...).run(
     *      listOf(
     *          automation("some") {
     *              // do somehting
     *          },
     *          automation("other") {
     *              // do something
     *          }
     *      )
     * )
     * ```
     * */
//    suspend fun run(functionalAutomations: Collection<FunctionalAutomation>) = run(functionalAutomations.map { it(this) })

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
//    suspend fun run(vararg automations: Automation) = run(automations.toList())

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    suspend fun run(vararg automations: Automation, mode: Mode = Mode.AUTOMATIC) {
        this.automations = automations.toList()
        println("KHomeAssistant started running at ${DateTime.nowLocal().utc}")
        if (secure) WebsocketsHttpClient.httpClient.wss(
            host = host,
            port = port,
            path = "/api/websocket",
            block = runOnWebsocket(mode)
        ) else WebsocketsHttpClient.httpClient.ws(
            host = host,
            port = port,
            path = "/api/websocket",
            block = runOnWebsocket(mode)
        )
    }

    /** What to run on the websocket */
    private fun runOnWebsocket(mode: Mode): suspend DefaultClientWebSocketSession.() -> Unit = {
        // authenticate on Home Assistant
        authenticate()

        // start the receiver thread
        startReceiver(ioScope = this)

        // start the sender thread
        startSender(ioScope = this)

        // update the initial state / attribute values in the cache
        updateCache()

        // tell Home Assistant we're interested in its events
        registerToEventBus()

        // initialize all user specified automations
        initializeAutomations()
        println("All automations are initialized")


        val couldStop = (stateListeners.isEmpty() || stateListeners.values.all { it.all { it.shortLived } })
                && scheduler.isEmpty
                && eventListeners.isEmpty()

        if (mode == Mode.JUST_INITIALIZE || mode == Mode.AUTOMATIC && couldStop) {
            println("There are no state listeners or scheduled tasks so KHomeAssistant is stopping...")
            receiver!!.cancelAndJoin()
            sender!!.cancelAndJoin()
        } else {
            println("There are ${stateListeners.values.sumBy { it.count { !it.shortLived } }} state listeners, ${scheduler.size} scheduled repeated tasks and ${eventListeners.size} event listeners, so KHomeAssistant keeps running...")

            // Heartbeat
            runEvery(30.seconds) {
                if (!connectionIsAlive()) {
                    println("Connection is not alive!")
                    connectionWasAlive = false
                } else if (!connectionWasAlive) {
                    connectionWasAlive = true
                    println("Connection alive!")
                }
            }

            receiver!!.join()
            sender!!.join()
        }
    }

    /** Authenticate with Home Assistant using the provided data. */
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

    /** Set [receiver] and start receiving. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun DefaultClientWebSocketSession.startReceiver(ioScope: CoroutineScope) {
        receiver = ioScope.launch {
            debugPrintln("receiver running!")
            while (reconnectOnClose) {
                while (true) {
                    if (incoming.isEmpty) {
                        canSend = true
                        canSendChannel.offer(Unit) // update the sender it's okay to send now if it was waiting before
                    }
                    // receive or wait, break if connection closed
                    val message = incoming.receiveOrNull() as? Frame.Text ?: break
                    canSend = false // incoming message, so stop the sender
                    ioScope.launch {
                        val json = message.readText()
                        debugPrintln(json)

                        val messageBase: MessageBase = fromJson(json)
                        val id = messageBase.id

                        when (messageBase.type) {
                            "result", "pong" -> responseAwaiters.remove(id)?.send(json)
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

                                        when {
                                            newState == null -> rawEntityData.remove(entityID)
                                            oldState == null -> rawEntityData[entityID] = newState
                                            else -> rawEntityData[entityID]!!.apply {
                                                state = newState.state
                                                attributes = newState.attributes
                                            }
                                        }

                                        stateListeners[entityID]?.forEach {
                                            this@KHomeAssistantInstance.launch {
                                                try {
                                                    it.listener(oldState, newState)
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

                                        debugPrintln("Detected call_service: $eventDataCallService")
                                        // TODO
                                    }
                                    // TODO maybe add more in the future
                                }

                                eventListeners[event.event_type]?.forEach {
                                    this@KHomeAssistantInstance.launch {
                                        try {
                                            it(event)
                                        } catch (e: Exception) {
                                            PrintException.print("Error happened after event $event fired.")
                                            throw e
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                println("Receiver channel closed${if (reconnectOnClose) ", restarting..." else ""}")
                if (reconnectOnClose) {
                    delay(5.seconds)
                    authenticate() // to be sure
                }
            }
        }
    }

    /** Dry [sender] and start sending. */
    private fun DefaultClientWebSocketSession.startSender(ioScope: CoroutineScope) {
        sender = ioScope.launch {
            while (true) {
                // receive from the sendQueue and send out
                val message = sendQueue.receive()
                while (!canSend) canSendChannel.receive() // if canSend is false, wait until it's set to true
                send(message)
                debugPrintln("Sent message: $message")
                delay(1) // just to give Home Assistant some peace of mind
            }
        }
    }

    /** Update [rawEntityData] from Home Assistant. */
    suspend fun updateCache() {
        val response: FetchStateResponse = sendMessage(FetchStateMessage())
        val newCache = hashMapOf<String, StateResult>()
        response.result!!.forEach {
            newCache[it.entity_id] = it
        }
        rawEntityData = newCache
        loadedInitialStates = true
        cacheAge = TimeSource.Monotonic.markNow()
        debugPrintln("Updated cache!")
    }

    /** Register to Home Assistant's event bus. */
    private suspend fun registerToEventBus() {
        val res: ResultMessageBase = sendMessage(
            SubscribeToEventMessage().also { debugPrintln(it) }
        )
        debugPrintln(
            if (res.success) "Successfully registered to event bus!"
            else "Error registering to event bus: ${res.result}"
        )
    }

    /** Initialize all automations asynchronously */
    private suspend fun initializeAutomations() {
        val automationInitialisations = hashSetOf<Job>()
        for (it in automations) this@KHomeAssistantInstance.launch {
            val inner = launch { it.initialize() }
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

    /** Cancel this task and schedule it again. */
    suspend fun reschedule(task: RepeatedTask) = scheduler.reschedule(task)

    /** Make this task be executed by the [scheduler]. */
    suspend fun schedule(task: RepeatedTask) = scheduler.schedule(task)

    /** Stop this task from being executed by the [scheduler]. */
    suspend fun cancel(task: RepeatedTask) = scheduler.cancel(task)

    /** This suspend function allows to send a message to Home Assistant and get the response.
     *
     * @param Send the type of [message] inheriting from [Message] you want to send
     * @param Response the type of response inheriting from [ResultMessage]
     * @param message the message of type [Send] you want to send
     * @param timeout optional timeout for a response, throws exception if exceeded
     * @return the response of type [Response] returned from Home Assistant
     * */
    private suspend inline fun <reified Send : Message, reified Response : ResultMessage> sendMessage(
        message: Send,
        timeout: TimeSpan? = null
    ): Response {
        // make sure the messages arrive in the queue in order
        messageIDMutex.withLock {
            message.id = ++messageID
            sendQueue.send(message.toJson())
        }

        // must wait for the results, or it's too fast for hass
        val receiveChannel = Channel<String?>(1)
        responseAwaiters[message.id] = receiveChannel

        if (timeout != null) runIn(timeout) {
            responseAwaiters.remove(message.id)?.send(null)
        }

        val responseString = receiveChannel.receive() ?: throw Exception("timeout exceeded")
        debugPrintln("Received result response: $responseString")

        return fromJson(responseString)
    }

    /**
     * Calls the given service on Home Assistant.
     * @param entity the entity that will be provided in the service data.
     * @param serviceDomain the domain of the service, like [nl.jolanrensen.kHomeAssistant.domains.Light] or [nl.jolanrensen.kHomeAssistant.domains.Switch]
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    override suspend fun callService(
        entity: Entity<*, *>,
        serviceDomain: Domain<*>,
        serviceName: String,
        data: JsonObject
    ): ResultMessage = callService(
        serviceDomain = serviceDomain.domainName,
        serviceName = serviceName,
        entityID = entity.entityID,
        data = data
    )

    /**
     * Calls the given service on Home Assistant.
     * @param serviceDomain the domain of the service, like [nl.jolanrensen.kHomeAssistant.domains.Light] or [nl.jolanrensen.kHomeAssistant.domains.Switch]
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    override suspend fun callService(serviceDomain: Domain<*>, serviceName: String, data: JsonObject): ResultMessage =
        callService(
            serviceDomain = serviceDomain.domainName,
            serviceName = serviceName,
            data = data
        )

    /**
     * Calls the given service on Home Assistant.
     * @param entity the entity that will be provided in the service data. Its domain will also be used as service domain.
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    override suspend fun callService(entity: Entity<*, *>, serviceName: String, data: JsonObject): ResultMessage =
        callService(
            serviceDomain = entity.domain.domainName,
            entityID = entity.entityID,
            serviceName = serviceName,
            data = data
        )

    /**
     * Calls the given service on Home Assistant.
     * @param serviceDomain the name of the domain of the service, like "light" or "switch"
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param entityID the optional entity ID of the entity passed to the service as data, like "light.my_lamp"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    override suspend fun callService(
        serviceDomain: String,
        serviceName: String,
        entityID: String?,
        data: JsonObject
    ): ResultMessage = sendMessage<CallServiceMessage, ResultMessageBase>(
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

    /**
     * Return the raw attributes of the given entity from Home Assistant.
     * @param EntityType the type of [entity]
     * @param entity the entity of type [EntityType] from which to get the attributes
     * @return the attributes of [entity] in the from of a [JsonObject]
     * @throws EntityNotInHassException if the entity provided cannot be found in Home Assistant
     */
    override fun <EntityType : Entity<*, *>> getRawAttributes(entity: EntityType): JsonObject =
        try {
            if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }
            rawEntityData[entity.entityID]!!.attributes
        } catch (e: Exception) {
            throw EntityNotInHassException("The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.")
        }

    /**
     * Return the state of the given entity from Home Assistant.
     * @param StateType the type of the state of [entity]
     * @param EntityType the type of [entity]
     * @param entity the entity of type [EntityType] from which to get the state
     * @return the state of entity of type [StateType]
     * @throws EntityNotInHassException if the entity provided cannot be found in Home Assistant
     * @throws Exception if the state cannot be parsed using `[entity].parseStateValue()`
     */
    override fun <StateType : Any, EntityType : Entity<StateType, *>> getState(entity: EntityType): StateType {
        if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }

        val stateValue = try {
            rawEntityData[entity.entityID]!!.state
        } catch (e: Exception) {
            throw EntityNotInHassException("The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.")
        }

        return try {
            entity.stringToState(stateValue)!!
        } catch (e: Exception) {
            throw Exception(
                "Could not parse state value \"$stateValue\" to entity with domain ${entity.domain::class.simpleName}, have you overridden the parseStateValue() function or are you perhaps querying the wrong entity?",
                e
            )
        }
    }

    /**
     * Return the context IDs for given [entity].
     * @param entity the entity to get the context for.
     * @return a [Context] instance.
     */
    override fun getContext(entity: Entity<*, *>): Context = try {
        if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }
        rawEntityData[entity.entityID]!!.context
    } catch (e: Exception) {
        throw EntityNotInHassException(
            "The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.",
            e
        )
    }

    /**
     * Return the last change time for the given [entity]
     * @param entity the entity to get the datetime for.
     * @return a [DateTime] instance
     */
    override fun getLastChanged(entity: Entity<*, *>): DateTime = try {
        if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }
        HASS_DATE_FORMAT.parseUtc(rawEntityData[entity.entityID]!!.last_changed)
    } catch (e: Exception) {
        throw EntityNotInHassException(
            "The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.",
            e
        )
    }

    /**
     * Return the last update time for the given [entity]
     * @param entity the entity to get the datetime for.
     * @return a [DateTime] instance
     */
    override fun getLastUpdated(entity: Entity<*, *>): DateTime = try {
        if (cacheAge.elapsedNow() > maxCacheAge) launch { updateCache() }
        HASS_DATE_FORMAT.parseUtc(rawEntityData[entity.entityID]!!.last_updated)
    } catch (e: Exception) {
        throw EntityNotInHassException(
            "The entity_id \"${entity.entityID}\" does not exist in your Home Assistant instance.",
            e
        )
    }

    /**
     * This will get a dump of the current config in Home Assistant.
     */
    override suspend fun getConfig(): HassConfig = sendMessage<GetConfig, GetConfigResult>(
        GetConfig()
    ).result

    /**
     * This will get a dump of the current services in Home Assistant.
     */
    override suspend fun getServices(): JsonObject = sendMessage<MessageBase, ResultMessageBase>(
        MessageBase(type = "get_services")
    ).result!!

    /**
     * This will get a dump of the current registered panels in Home Assistant.
     */
    override suspend fun getPanels(): JsonObject = sendMessage<MessageBase, ResultMessageBase>(
        MessageBase(type = "get_panels")
    ).result!!

    /**
     * Fetch a thumbnail picture for a media player.
     * @param mediaPlayer the media player entity to get the thumbnail for.
     * @return [NativeImage] that can be converted (on JVM) to AWT with `.toAwt()`.
     */
    override suspend fun getMediaPlayerThumbnail(mediaPlayer: MediaPlayer.Entity): NativeImage? =
        sendMessage<GetMediaPlayerThumbnail, GetMediaPlayerThumbnailResult>(
            GetMediaPlayerThumbnail(entity_id = mediaPlayer.entityID)
        ).result?.let {
            decodeImageBytes(Base64.decode(it.content))
        }

    override suspend fun connectionIsAlive(): Boolean = try {
        val time = measureTime {
            val result = sendMessage<Ping, Pong>(Ping(), 15.seconds)
            debugPrintln(result)
        }
        // TODO
        debugPrintln("ping pong took ${time.seconds} seconds")
        true
    } catch (e: Exception) {
        println(e)
        false
    }

    /** println's only executed if [debug] = true */
    override fun debugPrintln(message: Any?) {
        if (debug) println("DEBUG: $message")
    }
}