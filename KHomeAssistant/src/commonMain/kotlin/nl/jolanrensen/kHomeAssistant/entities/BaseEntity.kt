package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.HomeAssistant
import nl.jolanrensen.kHomeAssistant.helper.cast
import nl.jolanrensen.kHomeAssistant.messages.Context
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

typealias DefaultEntity = BaseEntity<String>

open class BaseEntity<StateType : Any>(
    open val kHomeAssistant: () -> KHomeAssistant? = { null },
    open val name: String,
    open val domain: Domain<*>
) {
    private var entityExists = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    open fun checkEntityExists() {
        if (entityExists) return
        kHomeAssistant?.invoke()?.launch {
            state // throws error if entity does not exist
            entityExists = true
        }
    }

    val attributes: ArrayList<KProperty0<*>> = arrayListOf()

    init {
        this.checkEntityExists()

        attributes += arrayOf(
            ::friendly_name,
            ::state,
            ::hidden,
            ::entity_picture,
            ::icon,
            ::assumed_state,
//            ::device_class,
            ::unit_of_measurement,
            ::initial_state,
            ::entityID,
            ::rawAttributes
        )
    }

    /** Given a string stateValue, this method should return the correct StateType */
    @OptIn(ExperimentalStdlibApi::class)
    open fun parseStateValue(stateValue: String): StateType? = try {
        stateValue as StateType
    } catch (e: Exception) {
        throw Exception("Did you forget to override parseStateValue() for ${domain.domainName}?")
    }

    /** This method returns the state for this entity in the original String format */
    open fun getStateValue(state: StateType): String? = try {
        state as String
    } catch (e: Exception) {
        throw Exception("Did you forget to override getStateValue() for ${domain.domainName}?")
    }

    open val state: StateType
        get() = kHomeAssistant()!!.getState(this)
    // set() TODO

    suspend fun setState(s: StateType): Unit = TODO()

    val rawAttributes: JsonObject
        get() = kHomeAssistant()!!.getAttributes(this)

    val attrsDelegate = object : AttributesDelegate {
        override operator fun <V : Any?> getValue(thisRef: Any, property: KProperty<*>): V? =
            rawAttributes[property.name]?.cast(property.returnType)
    }

    // Default attributes

    /** Name of the entity as displayed in the UI. */
    val friendly_name: String? by attrsDelegate

    /** Is true if the entity is hidden. */
    val hidden: Boolean get() = attrsDelegate.getValue(this, ::hidden) ?: false

    /** URL used as picture for entity. */
    val entity_picture: Boolean? by attrsDelegate

    /** Icon used for this enitity. Usually of the kind "mdi:icon" */
    val icon: String? by attrsDelegate

    /** For switches with an assumed state two buttons are shown (turn off, turn on) instead of a switch. If assumed_state is false you will get the default switch icon. */
    val assumed_state: Boolean get() = attrsDelegate.getValue(this, ::assumed_state) ?: true

//    /** The class of the device as set by configuration, changing the device state and icon that is displayed on the UI (see below). It does not set the unit_of_measurement.*/
//    val device_class: String? by attrsDelegate // TODO maybe move to binary sensor, sensor, cover and media player only

    /** Defines the units of measurement, if any. This will also influence the graphical presentation in the history visualisation as continuous value. Sensors with missing unit_of_measurement are showing as discrete values. */
    val unit_of_measurement: String? by attrsDelegate

    /** Defines the initial state for automations, on or off. */
    val initial_state: String? by attrsDelegate


    suspend fun getLastChanged(): String = TODO("last_changed uit State")
    suspend fun getLastUpdated(): String = TODO("last_updated uit State")
    suspend fun getContext(): Context = TODO("context uit State")

    /** Request the update of an entity, rather than waiting for the next scheduled update, for example Google travel time sensor, a template sensor, or a light */
    suspend inline fun updateEntity() = callService(HomeAssistant, "update_entity")

    /** Call a service with this entity using a different serviceDomain */
    suspend fun callService(
        serviceDomain: Domain<*>,
        serviceName: String,
        data: Map<String, JsonElement> = mapOf(),
        doEntityCheck: Boolean = true
    ): ResultMessage {
        if (doEntityCheck) checkEntityExists()
        return kHomeAssistant()!!.callService(
            entity = this,
            serviceDomain = serviceDomain,
            serviceName = serviceName,
            data = data
        )
    }

    suspend fun callService(
        serviceName: String,
        data: Map<String, JsonElement> = mapOf(),
        doEntityCheck: Boolean = true
    ): ResultMessage {
        if (doEntityCheck) checkEntityExists()
        return kHomeAssistant()!!.callService(
            entity = this,
            serviceName = serviceName,
            data = data
        )
    }

    val entityID: String
        get() = "${domain.domainName}.$name"

    override fun toString() = "${domain::class.simpleName}.Entity($name) {${
    attributes
        .filter { it.get() != null }
        .map { "\n    ${it.name} = ${it.get()}" }
        .toString()
        .run { subSequence(1, length - 1) }
    }\n}"

}

interface AttributesDelegate {
    operator fun <V : Any?> getValue(thisRef: Any, property: KProperty<*>): V?
}


/** Used to get attributes using a delegate. */
//inline operator fun <reified Entity: BaseEntity<*>, reified V: Any?> Attributes.getValue(baseEntity: Entity, property: KProperty<*>): V? =
//    get(property.name)?.cast()


///** Used to get attributes using a delegate. */
//inline operator fun <S : Any, E : BaseEntity<S>, reified V : Any?> E.getValue(
//    thisRef: BaseEntity<*>?,
//    property: KProperty<*>
//): V? = attributes[property.name]?.cast()

/** Shorthand for apply, allows for DSL-like behavior on entities. */
inline operator fun <S : Any, E : BaseEntity<S>> E.invoke(callback: E.() -> Unit): E = apply(callback)

/** Shorthand for apply for each, allows for DSL-like behavior on collections of entities. */
inline operator fun <S : Any, E : BaseEntity<S>> Iterable<E>.invoke(callback: E.() -> Unit): Iterable<E> =
    apply { forEach(callback) }


fun <S : Any, E : BaseEntity<S>> E.onStateChangedToNot(
    newState: S,
    callback: suspend E.() -> Unit
): E {
    onStateChanged { it ->
        if (newState != it)
            callback()
    }
    return this
}

fun <S : Any, E : BaseEntity<S>> E.onStateChangedTo(
    newState: S,
    callback: suspend E.() -> Unit
): E {
    onStateChanged { it ->
        if (newState == it)
            callback()
    }
    return this
}

fun <S : Any, E : BaseEntity<S>> E.onStateChanged(
    callback: suspend E.(newState: S?) -> Unit
): E {
    checkEntityExists()
    kHomeAssistant()!!.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add { oldState, newState ->
            if (oldState.state != newState.state)
                callback(parseStateValue(newState.state))
        }
    return this
}

fun <S : Any, E : BaseEntity<S>> E.onAttributesChanged(
    callback: suspend E.(newAttributes: JsonObject?) -> Unit
): E {
    checkEntityExists()
    kHomeAssistant()!!.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add { oldState, newState ->
            if (oldState.attributes != newState.attributes)
                callback(newState.attributes)
        }
    return this
}