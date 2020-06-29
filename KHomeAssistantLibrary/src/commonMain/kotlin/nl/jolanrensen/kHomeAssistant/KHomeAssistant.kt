package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.korim.bitmap.NativeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.core.StateListener
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.entities.EntityNotInHassException
import nl.jolanrensen.kHomeAssistant.messages.Context
import nl.jolanrensen.kHomeAssistant.messages.Event
import nl.jolanrensen.kHomeAssistant.messages.HassConfig
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

interface KHomeAssistant : CoroutineScope {

    var loadedInitialStates: Boolean

    /** Reference to the current KHomeAssistant instance */
    val instance: KHomeAssistantInstance

    /** Returns all the raw entity IDs known in Home Assistant. */
    val entityIds: Set<String>

    /** Returns all entities in Home Assistant as [DefaultEntity] (`= Entity<String>`) (since we cannot know all possible types of entities) */
    val entities: List<DefaultEntity>

    /** HA version reported by the connected instance */
    var haVersion: String

    /** All the event listeners, `eventListeners["event"] =` set of listeners for this event type. */
    val eventListeners: HashMap<String, HashSet<suspend (Event) -> Unit>>

    /** All the state/attribute listeners. `stateListeners["entity_id"] =` set of listeners for this entity_id */
    val stateListeners: HashMap<String, HashSet<StateListener>>

    /** If enabled, debug messages will be printed. */
    val debug: Boolean

    /** Performs a ping/pong to see if the Home Assistant is still connected. */
    suspend fun connectionIsAlive(): Boolean

    /**
     * Calls the given service on Home Assistant.
     * @param entity the entity that will be provided in the service data.
     * @param serviceDomain the domain of the service, like [nl.jolanrensen.kHomeAssistant.domains.Light] or [nl.jolanrensen.kHomeAssistant.domains.Switch]
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    suspend fun callService(
        entity: Entity<*, *>,
        serviceDomain: Domain<*>,
        serviceName: String,
        data: JsonObject = json { }
    ): ResultMessage

    /**
     * Calls the given service on Home Assistant.
     * @param serviceDomain the domain of the service, like [nl.jolanrensen.kHomeAssistant.domains.Light] or [nl.jolanrensen.kHomeAssistant.domains.Switch]
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    suspend fun callService(serviceDomain: Domain<*>, serviceName: String, data: JsonObject = json { }): ResultMessage

    /**
     * Calls the given service on Home Assistant.
     * @param entity the entity that will be provided in the service data. Its domain will also be used as service domain.
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    suspend fun callService(entity: Entity<*, *>, serviceName: String, data: JsonObject = json { }): ResultMessage

    /**
     * Calls the given service on Home Assistant.
     * @param serviceDomain the name of the domain of the service, like "light" or "switch"
     * @param serviceName the name of the service to call from the domain, like "turn_on"
     * @param entityID the optional entity ID of the entity passed to the service as data, like "light.my_lamp"
     * @param data the optional [JsonObject] or [Map]<[String], [JsonElement]> containing the extra data for the service
     * @return the result in form of a [ResultMessage]
     * */
    suspend fun callService(
        serviceDomain: String,
        serviceName: String,
        entityID: String? = null,
        data: JsonObject = json { }
    ): ResultMessage

    /**
     * Return the raw attributes of the given entity from Home Assistant.
     * @param EntityType the type of [entity]
     * @param entity the entity of type [EntityType] from which to get the attributes
     * @return the attributes of [entity] in the from of a [JsonObject]
     * @throws EntityNotInHassException if the entity provided cannot be found in Home Assistant
     */
    fun <EntityType : Entity<*, *>> getRawAttributes(entity: EntityType): JsonObject

    /**
     * Return the state of the given entity from Home Assistant.
     * @param StateType the type of the state of [entity]
     * @param EntityType the type of [entity]
     * @param entity the entity of type [EntityType] from which to get the state
     * @return the state of entity of type [StateType]
     * @throws EntityNotInHassException if the entity provided cannot be found in Home Assistant
     * @throws Exception if the state cannot be parsed using `[entity].parseStateValue()`
     */
    fun <StateType : Any, EntityType : Entity<StateType, *>> getState(entity: EntityType): StateType

    /**
     * Return the context IDs for given [entity].
     * @param entity the entity to get the context for.
     * @return a [Context] instance.
     */
    fun getContext(entity: Entity<*, *>): Context

    /**
     * Return the last change time for the given [entity]
     * @param entity the entity to get the datetime for.
     * @return a [DateTime] instance
     */
    fun getLastChanged(entity: Entity<*, *>): DateTime

    /**
     * Return the last update time for the given [entity]
     * @param entity the entity to get the datetime for.
     * @return a [DateTime] instance
     */
    fun getLastUpdated(entity: Entity<*, *>): DateTime

    /**
     * This will get a dump of the current config in Home Assistant.
     */
    suspend fun getConfig(): HassConfig

    /**
     * This will get a dump of the current services in Home Assistant.
     */
    suspend fun getServices(): JsonObject

    /**
     * Fetch a thumbnail picture for a media player.
     */
    suspend fun getMediaPlayerThumbnail(mediaPlayer: MediaPlayer.Entity): NativeImage?

    /**
     * This will get a dump of the current registered panels in Home Assistant.
     */
    suspend fun getPanels(): JsonObject

    /** println's only executed if `[debug] == true` */
    fun debugPrintln(message: Any?)
}

