@file:Suppress("FunctionName")

package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

/**
 * Interface where all domains (like [Light], [MediaPlayer], [Switch]) inherit from.
 * @param E the type of entity this domain has. By default (or for a domain without entity) you can use [DefaultEntity].
 */
interface Domain<out E : Entity<*, *>> {

    /** The Home Assistant name for this domain, like "light". */
    val domainName: String

    /** Function to access the [KHomeAssistantInstance] instance. This is passed around [KHomeAssistant] inheritors,
     * usually in their constructor. */
    val kHassInstance: KHomeAssistant

    /** Function to create an entity instance in a domain with correct context.
     * @param name the name of the entity (without domain) as defined in Home Assistant
     * @return an instance of the entity
     * @see [get]
     * @see [getValue]
     * */
    fun Entity(name: String): E


    /** Helper function to create multiple entity instances at once in a domain.
     * @param names the names of the entities (without domain) as defined in Home Assistant
     * @return a list of the instances of the entities
     * */
    fun Entities(vararg names: String): List<E> = names.map { Entity(it) }

    // TODO maybe allow a way to make an anonymous toggle entity

    /**
     * Shorthand to be able to type `YourDomain["entity"]` instead of `YourDomain.Entity("entity")`.
     * @param name the name of the entity (without domain) as defined in Home Assistant
     * @return an instance of the entity
     * @see [Entity]
     * @see [getValue]
     * */
    operator fun get(name: String): E = Entity(name)

    /** Shorthand to be able to type `YourDomain["entity", "other_entity"]` instead of `YourDomain.Entities("entity", "other_entity")`.
     * @param names the names of the entities (without domain) as defined in Home Assistant
     * @return a list of the instances of the entities
     * @see [Entities]
     * */
    operator fun get(name: String, vararg names: String): List<E> = Entities(name, *names)

    /**
     * Call a service with an entity and data.
     * For instance, turning on a light would be:
     * ```kotlin
     * Light.Entity("kitchen").callService(serviceName = "turn_on", data = mapOf("brightness" to JsonPrimitive(100)))
     * ```
     * @param serviceName the name of the service in Home Assistant (like "turn_on")
     * @param data a map of [String] to [JsonElement] that will be sent to Home Assistant along with the command
     * @return a [ResultMessage] containing the results of the call
     * */
    suspend fun callService(serviceName: String, data: JsonObject = buildJsonObject { }): ResultMessage =
        kHassInstance.callService(
            serviceDomain = this,
            serviceName = serviceName,
            data = data
        )
}

/** Shorthand for [apply] for each, allows for DSL-like behavior on a bunch of newly instanced entities.
 * For example:
 * ```kotlin
 * YourDomain.Entities("some_name", "some_other_name") {
 *    someAttribute = 100
 *    someValue = someOtherValue!! + 1
 * }
 * ```
 * @param names the names of the entities (without domain) as defined in Home Assistant
 * @param callback the block of code that is executed on each of the entities with the entity as this
 * @return a list of the instances of the entities
 * */
inline fun <E : Entity<*, *>> Domain<E>.Entities(vararg names: String, callback: E.() -> Unit): List<E> =
    Entities(*names).apply { forEach(callback) }

/** Shorthand for [apply], allows for DSL-like behavior on a newly instanced entity.
 * For example:
 * ```kotlin
 * YourDomain.Entity("some_name") {
 *    someAttribute = 100
 *    someValue = someOtherValue!! + 1
 * }
 * ```
 * @param name the name of the entity (without domain) as defined in Home Assistant
 * @param callback the block of code that is executed with the entity as this
 * @return an instance of the entity
 * */
inline fun <E : Entity<*, *>> Domain<E>.Entity(name: String, callback: E.() -> Unit): E = Entity(name).apply(callback)

/**
 * Create a temporary [Domain]. Useful for quick service calls or for by KHomeAssistant unsupported domains.
 * For example:
 * ```kotlin
 * Domain("some_domain").Entity("some_entity").callService("some_service")
 * Domain("some_domain").callService("some_service")
 * ```
 * @receiver any [KHomeAssistant] inheriting class like [nl.jolanrensen.kHomeAssistant.Automation]
 * @param domainName the Home Assistant name for this domain, like "light"
 * @return a [Domain] inheriting object with [DefaultEntity] as its entity
 **/
fun KHomeAssistant.Domain(domainName: String): Domain<DefaultEntity> =
    object : Domain<DefaultEntity> {
        override val domainName = domainName
        override var kHassInstance: KHomeAssistant = this@Domain

        override fun Entity(name: String): DefaultEntity =
            object : DefaultEntity(kHassInstance = kHassInstance, name = name, domain = this) {
                override fun stringToState(stateValue: String) = stateValue
                override fun stateToString(state: String) = state
            }

        /** Making sure Domain acts as a singleton. */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Domain<*>

            if (domainName != other.domainName) return false

            return true
        }

        override fun hashCode(): Int = domainName.hashCode()
    }

/**
 * Alternative to creating defining an entity.
 * @see [Domain.Entity]
 * @see [Domain.get]
 *
 * For example:
 * ```
 * val my_light by Light
 * ```
 */
operator fun <E : Entity<*, *>> Domain<E>.getValue(thisRef: Any?, property: KProperty<*>): E = Entity(property.name)
