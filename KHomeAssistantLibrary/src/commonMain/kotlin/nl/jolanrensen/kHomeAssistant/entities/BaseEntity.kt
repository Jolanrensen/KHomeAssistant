package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.SceneEntityState
import nl.jolanrensen.kHomeAssistant.cast
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.HomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Scene
import nl.jolanrensen.kHomeAssistant.messages.Context
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

typealias DefaultEntity = BaseEntity<String>

/** Type returned when using myEntity::myAttribute */
typealias Attribute<A> = KProperty0<A>

/** Type returned when using SomeDomain.Entity::myAttribute */
typealias NonSpecificAttribute<E, A> = KProperty1<E, A>

open class BaseEntity<StateType : Any>(
    override val getKHass: () -> KHomeAssistant? = { null },
    open val name: String,
    open val domain: Domain<*>
) : HasKHassContext {

    override val coroutineContext: CoroutineContext
        get() = getKHass()!!.coroutineContext

    private var entityExists = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    open fun checkEntityExists() {
        if (entityExists) return
        getKHass?.invoke()?.apply {
            launch {
                if (entityID !in entityIds)
                    throw EntityNotInHassException("The entity_id \"$entityID\" does not exist in your Home Assistant instance.")
                entityExists = true
            }
        }
    }

    val attributes: ArrayList<Attribute<*>> = arrayListOf()

    init {
        this.checkEntityExists()

        attributes += arrayOf(
            ::friendly_name,
            ::state,
            ::hidden,
            ::entity_picture,
            ::icon,
            ::assumed_state,
            ::device_class,
            ::unit_of_measurement,
            ::initial_state,
            ::entityID,
            ::rawAttributes
        )
    }

    /** Given a string stateValue, this method should return the correct StateType */
    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalStdlibApi::class)
    open fun stringToState(stateValue: String): StateType? = try {
        stateValue as StateType
    } catch (e: Exception) {
        throw Exception("Did you forget to override parseStateValue() for ${domain.domainName}?")
    }

    /** This method returns the state for this entity in the original String format */
    open fun stateToString(state: StateType): String? = try {
        state as String
    } catch (e: Exception) {
        throw Exception("Did you forget to override getStateValue() for ${domain.domainName}?")
    }

    /** State as String as defined in Home Assistant. */
    val stateAsString: String // TODO
        get() = stateToString(state)!!

    /** State of the entity. */
    open var state: StateType
        get() = getKHass()!!.getState(this)
        @Deprecated(
            level = DeprecationLevel.WARNING,
            message = "Uses Scene.apply, so it isn't guaranteed to work. Use functions inside a domain to be more certain."
        )
        set(value) {
            println("Attempting to set state of $entityID using Scene.apply")
            runBlocking {
                Scene.apply(SceneEntityState(this@BaseEntity, value))
                suspendUntilStateChangedTo(value)
            }
        }
    // set() TODO?

//    suspend fun setState(s: StateType): Unit = TODO()

    /** Get the raw attributes from Home Assistant in json format.
     * Raw attributes can also directly be obtained using yourEntity["raw_attribute"] */
    val rawAttributes: JsonObject
        get() = getKHass()!!.getAttributes(this)

    /** Helper function to get raw attributes in json format using yourEntity["attribute"]
     * @see [BaseEntity.rawAttributes]
     *
     * For example:
     * ```
     * val attrValue: String = myEntity["my_attribute"]!!.content
     * ``` */
    operator fun get(name: String): JsonElement? = rawAttributes[name]

    /** ONLY USE TEMPORARILY */
    internal var alternativeAttributes: JsonObject? = null

    /** Makes delegated attributes possible for entities.
     * It tries to take the name of the variable as attribute name and cast the result
     * to the type of the variable.
     * @throws IllegalArgumentException when the attribute can't be found
     * */
    fun <V : Any?> attrsDelegate(): AttributesDelegate<V> = object : AttributesDelegate<V> {
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): V =
            (alternativeAttributes ?: rawAttributes)[property.name]
                .let { it ?: throw IllegalArgumentException("Couldn't find attribute ${property.name}") }
                .cast(property.returnType)
    }

    /** Makes delegated attributes possible for entities.
     * It tries to take the name of the variable as attribute name and cast the result
     * to the type of the variable.
     * @param default is returned when attribute cannot be found or cast
     * */
    fun <V : Any?> attrsDelegate(default: V): AttributesDelegate<V> = object : AttributesDelegate<V> {
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): V =
            when (val attr = (alternativeAttributes ?: rawAttributes)[property.name]) {
                null -> default
                else -> try {
                    attr.cast<V>(property.returnType)
                } catch (e: Exception) {
                    default
                }
            }
    }

    // Default attributes

    /** Name of the entity as displayed in the UI. */
    val friendly_name: String by attrsDelegate()

    /** Is true if the entity is hidden. */
    val hidden: Boolean by attrsDelegate(false)

    /** URL used as picture for entity. */
    val entity_picture: String by attrsDelegate()

    /** Icon used for this enitity. Usually of the kind "mdi:icon" */
    val icon: String by attrsDelegate()

    /** For switches with an assumed state two buttons are shown (turn off, turn on) instead of a switch. If assumed_state is false you will get the default switch icon. */
    val assumed_state: Boolean by attrsDelegate(true)

    //    /** The class of the device as set by configuration, changing the device state and icon that is displayed on the UI (see below). It does not set the unit_of_measurement.*/
    val device_class: String by attrsDelegate() // TODO maybe move to binary sensor, sensor, cover and media player only

    /** Defines the units of measurement, if any. This will also influence the graphical presentation in the history visualisation as continuous value. Sensors with missing unit_of_measurement are showing as discrete values. */
    val unit_of_measurement: String? by attrsDelegate(null)

    /** Defines the initial state for automations, on or off. */
    val initial_state: String by attrsDelegate()

    /** ID given by Home Assistant if the entity was created from the UI instead of the yaml. */
    val id: String by attrsDelegate()


    suspend fun getLastChanged(): String = TODO("last_changed uit State")
    suspend fun getLastUpdated(): String = TODO("last_updated uit State")
    suspend fun getContext(): Context = TODO("context uit State")

    /** Request the update of an entity, rather than waiting for the next scheduled update, for example Google travel time sensor, a template sensor, or a light */
    suspend inline fun updateEntity() = callService(HomeAssistant(getKHass), "update_entity")

    /** Call a service with this entity using a different serviceDomain */
    suspend fun callService(
        serviceDomain: Domain<*>,
        serviceName: String,
        data: JsonObject = json { },
        doEntityCheck: Boolean = true
    ): ResultMessage {
        if (doEntityCheck) checkEntityExists()
        return getKHass()!!.callService(
            entity = this,
            serviceDomain = serviceDomain,
            serviceName = serviceName,
            data = data
        )
    }

    suspend fun callService(
        serviceName: String,
        data: JsonObject = json { },
        doEntityCheck: Boolean = true
    ): ResultMessage {
        if (doEntityCheck) checkEntityExists()
        return getKHass()!!.callService(
            entity = this,
            serviceName = serviceName,
            data = data
        )
    }

    /** Shortcut to name of domain for this entity. */
    val domainName: String
        get() = domain.domainName

    /** Entity ID as defined in Home Assistant.
     * Like "light.light_name". */
    val entityID: String
        get() = "$domainName.$name"

    override fun toString() = "${domain::class.simpleName} ($domainName) Entity($name) {${
    attributes
        .filter {
            try {
                it.get()
            } catch (e: Exception) {
                null
            } != null
        }
        .map { "\n    ${it.name} = ${it.get()}" }
        .toString()
        .run { subSequence(1, length - 1) }
    }\n}"

    /** An entity is defined by its domain and name. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BaseEntity<*>

        if (name != other.name) return false
        if (domain != other.domain) return false

        return true
    }

    /** An entity is defined by its domain and name. */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }

}

interface AttributesDelegate<V : Any?> {
    /** Makes delegated attributes possible for entities. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): V

    /** Makes delegated attributes possible for entities. Uses Scene.apply to try and set the value of the attribute. */
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
//        if (onState == null)
//            throw IllegalArgumentException("onState is needed to attempt to use Scene.apply to set the value of an attribute.")
//
//        val jsonValue = when (value) {
//            is Number, is Number? -> JsonPrimitive(value as Number?)
//            is Boolean, is Boolean? -> JsonPrimitive(value as Boolean?)
//            is String, is String? -> JsonPrimitive(value as String?)
//
//            else -> throw IllegalArgumentException("value must be a number, string or boolean, else it can't be turned into a JsonPrimitive")
//        }
//        runBlocking {
//            entity.Scene.apply(SceneEntityState(
//                entity = entity,
//                state = onState,
//                attributes = json { property.name to jsonValue }
//            ))
//        }
//    }
}

/**
 * Shorthand for apply, allows for DSL-like behavior on entities.
 *
 * For example:
 * ```
 * myLight {
 *     color = Colors.BLUE
 *     white_value = 125
 * }
 * ```
 * */
inline operator fun <S : Any, E : BaseEntity<S>> E.invoke(callback: E.() -> Unit): E = apply(callback)

/**
 * Shorthand for apply for each, allows for DSL-like behavior on collections of entities.
 *
 * For example:
 * ```
 * myListOfLights {
 *     color = Colors.BLUE
 *     white_value = 125
 * }
 * ```
 * */
inline operator fun <S : Any, E : BaseEntity<S>> Iterable<E>.invoke(callback: E.() -> Unit): Iterable<E> =
    apply { forEach(callback) }

//suspend fun <S : Any, E : BaseEntity<S>> Iterable<E>.callService(
//    serviceDomain: Domain<*>,
//    serviceName: String,
//    data: JsonObject = mapOf(),
//    doEntityCheck: Boolean = true
//): ResultMessage {
//    if (doEntityCheck) forEach { it.checkEntityExists() }
//
//    return serviceDomain.getKHomeAssistant()!!.callService(
//        serviceDomain = serviceDomain,
//        serviceName = serviceName,
//        data = data + json {
//
//        }
//    )
//}
