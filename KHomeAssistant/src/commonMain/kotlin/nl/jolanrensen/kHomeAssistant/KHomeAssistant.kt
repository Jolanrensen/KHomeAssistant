package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.DateTime
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.Clock.cancelAllTimers
import nl.jolanrensen.kHomeAssistant.WebsocketsHttpClient.httpClient
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.helper.PriorityQueue
import nl.jolanrensen.kHomeAssistant.helper.priorityQueueOf
import nl.jolanrensen.kHomeAssistant.messages.*
import kotlin.jvm.Volatile
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.minutes

/**
 * KHomeAssistant instance.
 * This is used to connect to your Home Assistant server and start running the provided automations.
 * Run [run] from a suspended context (a suspend fun or in a `runBlocking { }` coroutine scope).
 *
 * @param host the address on which to reach your Home Assistant server. Like "https://myHomeAssistant.com"
 * @param port the port for your Home Assistant (API), usually 8123
 * @param accessToken the Long-lived Access Token that can be generated from your Home Assistant profile settings
 * @param secure whether to connect over SSL or not (https or http)
 * @param debug if enabled, debug messages will be printed
 * @param automations a collection of [Automation] instances that should be run by [KHomeAssistant]
 */
@OptIn(ExperimentalTime::class)
class KHomeAssistant(
    /** The address on which to reach your Home Assistant server. Like "https://myHomeAssistant.com". */
    val host: String,

    /** The port for your Home Assistant (API), usually 8123. */
    val port: Int = 8123,

    /** The Long-lived Access Token that can be generated from your Home Assistant profile settings. */
    val accessToken: String,

    /** Whether to connect over SSL or not (https or http). */
    val secure: Boolean = false,

    /** If enabled, debug messages will be printed. */
    val debug: Boolean = false,

    /** A collection of [Automation] instances that should be run by [KHomeAssistant]. */
    val automations: Collection<Automation>
) : KHomeAssistantContext {

    /**
     * KHomeAssistant instance.
     * This is used to connect to your Home Assistant server and start running the provided automations.
     * Run [run] from a suspended context (a suspend fun or in a runBlocking { } coroutine scope).
     * This constructor is a shorthand for an instance with a single automation:
     * ```
     * KHomeAssistant(
     *     // params
     * ) { // this: Automation
     *    // execute something
     * }.run()
     * ```
     * @param host the address on which to reach your Home Assistant server. Like "https://myHomeAssistant.com"
     * @param port the port for your Home Assistant (API), usually 8123
     * @param accessToken the Long-lived Access Token that can be generated from your Home Assistant profile settings
     * @param secure whether to connect over SSL or not (https or http)
     * @param debug if enabled, debug messages will be printed
     * @param automationName the name of the single automation defined in [automation]
     * @param automation the automation [Automation.initialize] function defining the automation
     */
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

    /** The function providing the kHomeAssistant instace as context to other objects. */
    override val kHomeAssistant = { this }

    /** All exceptions from couroutines in this scope will be handled here. */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PrintException.print("", throwable)
    }

    /** Makes sure that separate coroutines can fail independently. */
    private val supervisor = SupervisorJob()

    /** The context of the current coroutinescope */
    override val coroutineContext = Dispatchers.Default + supervisor + exceptionHandler

    /** The cache for the state and attributes of all entities in Home Assistant. */
    @Volatile
    private var cache: HashMap<String, StateResult> = hashMapOf()

    /** The age of the [cache]. */
    private var cacheAge = TimeSource.Monotonic.markNow()

    /** The time after which to do a full refresh of the state/attributes of all entities.
     * Usually KHomeAssistant relies on state change events. */
    private val maxCacheAge = 15.minutes

    /** Update [cache] from Home Assistant. */
    private suspend fun updateCache() {
        val response: FetchStateResponse = sendMessage(FetchStateMessage())
        val newCache = hashMapOf<String, StateResult>()
        response.result!!.forEach {
            newCache[it.entity_id] = it
        }
        cache = newCache
        cacheAge = TimeSource.Monotonic.markNow()
        debugPrintln("Updated cache!")
    }

    /** HA version reported by the connected instance */
    lateinit var haVersion: String

    /** ID to represent the number of interactions with the HA instance. Used in [sendMessage]. */
    private var messageID: Int = 0

    /** Mutex that has to be used to modify [messageID] to prevent race conditions. */
    private val messageIDMutex = Mutex()

    /** println's only executed if [debug] = true */
    fun debugPrintln(message: Any?) {
        if (debug) println("DEBUG: $message")
    }

    /** stateListeners["entity_id"] = set of listeners for this entity_id */
    val stateListeners: HashMap<String, HashSet<StateListener>> = hashMapOf()

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

    /** What to run on the websocket */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val runOnWebsocket: suspend DefaultClientWebSocketSession.() -> Unit = {
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

                    when (messageBase.type) {
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

    /** Run KHomeAssistant, this makes the connection, authenticates, initializes and runs the complete HA interaction */
    suspend fun run() {
        println("KHomeAssistant started running at ${DateTime.nowLocal().utc}")
        if (secure) httpClient.wss(
            host = host,
            port = port,
            path = "/api/websocket",
            block = runOnWebsocket
        ) else httpClient.ws(
            host = host,
            port = port,
            path = "/api/websocket",
            block = runOnWebsocket
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

    private val newScheduler
        get() = launch {
            while (scheduledRepeatedTasks.isNotEmpty()) {
                // Get next scheduled task in the future
                val next = scheduledRepeatedTasksLock.withLock {
                    if (scheduledRepeatedTasks.isEmpty())
                        null
                    else
                        scheduledRepeatedTasks.next
                } ?: break // break if there are no tasks left

                // Suspend until it's time to execute the next task (can be canceled here)
                delay((next.scheduledNextExecution - DateTime.now()).millisecondsLong.also {
                    debugPrintln("Waiting for $it milliseconds until the next scheduled execution")
                })

                // check whether the next task isn't canceled in the meantime
                if (scheduledRepeatedTasksLock.withLock {
                        scheduledRepeatedTasks.isEmpty() || next != scheduledRepeatedTasks.next
                    }) continue

                // remove it from the schedule and execute
                this@KHomeAssistant.launch { next.callback() }

                // check for a reschedule, probably not needed for RepeatedIrregularTask
                next.update()
            }
            scheduler = null
        }

    private var scheduler: Job? = null

    private val scheduledRepeatedTasks: PriorityQueue<RepeatedTask> = priorityQueueOf()
    private val scheduledRepeatedTasksLock = Mutex()

    suspend fun reschedule(task: RepeatedTask) {
        cancel(task)
        schedule(task)
    }

    suspend fun schedule(task: RepeatedTask) {
        scheduledRepeatedTasksLock.withLock {
            if (scheduledRepeatedTasks.isEmpty() || task < scheduledRepeatedTasks.next) {
                scheduler?.cancel()
                scheduledRepeatedTasks += task
                scheduler = newScheduler
            } else {
                scheduledRepeatedTasks += task
            }
        }
    }

    suspend fun cancel(task: RepeatedTask) {
        scheduledRepeatedTasksLock.withLock {
            scheduledRepeatedTasks -= task
        }
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

        return try {
            entity.parseStateValue(stateValue)!!
        } catch (e: Exception) {
            throw Exception(
                "Could not parse state value \"$stateValue\" to entity with domain ${entity.domain::class.simpleName}, have you overridden the parseStateValue() function or are you perhaps querying the wrong entity?",
                e
            )
        }
    }

//    suspend fun getMediaPlayerThumbnail(mediaPlayer: MediaPlayer.Entity): Array
}

typealias StateListener = suspend (oldState: StateResult, newState: StateResult) -> Unit