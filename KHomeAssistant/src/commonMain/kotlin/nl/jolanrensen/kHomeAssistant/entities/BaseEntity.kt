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

typealias DefaultEntity = BaseEntity<String>

open class BaseEntity<StateType : Any>(
    open val kHomeAssistant: () -> KHomeAssistant? = { null },
    open val name: String,
    open val domain: Domain<out BaseEntity<out StateType>>
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

    init {
        this.checkEntityExists()
    }

    /** Given a string stateValue, this method should return the correct StateType */
    open fun parseStateValue(stateValue: String): StateType? = null

    /** This method returns the state for this entity in the original String format */
    open fun getStateValue(state: StateType): String? = null

    val state: StateType
        get() = kHomeAssistant()!!.getState(this)
    // set() TODO

    suspend fun setState(s: StateType): Unit = TODO()

    val attributes: JsonObject
        get() = kHomeAssistant()!!.getAttributes(this)

    val friendly_name: String? by this

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

}

/** Used to get attributes using a delegate. */
inline operator fun <S : Any, E : BaseEntity<S>, reified V : Any?> E.getValue(
    thisRef: BaseEntity<*>?,
    property: KProperty<*>
): V? = attributes[property.name]?.cast()

/** Shorthand for apply, allows for DSL-like behavior on entities. */
inline operator fun <S : Any, E : BaseEntity<S>> E.invoke(callback: E.() -> Unit): E = apply(callback)

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

/**
 * All entities can be created without KHomeAssistant instance from within an Automation
 * and other classes having an instance as kHomeAssistant can be accessed through there anyways.
 * */
//inline fun <reified StateType : Any, reified AttributesType : Attributes> KHomeAssistantContext.Entity(domain: Domain<out Entity<out StateType, out AttributesType>>, name: String): Entity<StateType, AttributesType> =
//        Entity<StateType, AttributesType>(
//                kHomeAssistant = kHomeAssistant,
//                domain = domain,
//                name = name
//        )

//fun KHomeAssistantContext.Entity(domain: Domain, name: String) =
//        Entity<Any, Attributes>(
//                domain = domain,
//                name = name
//        )


// TODO probably remove this
//inline fun <reified StateType : Any, reified AttributesType : Attributes>
//        KHomeAssistantContext.Entity(domainName: String, name: String): Entity<StateType, AttributesType> {
//    var e: Entity<StateType, AttributesType>? = null
//    e = Entity(
//            kHomeAssistant = kHomeAssistant,
//            name = name,
//            domain = Domain(domainName)
//    )
//    return e
//}

//fun KHomeAssistantContext.Entity(domainName: String, name: String) =
//        Entity<Any, Attributes>(
//                domainName = domainName,
//                name = name
//        )


//inline fun <reified StateType : Any, reified AttributesType : Attributes> KHomeAssistantContext.Entity(entityID: String): Entity<StateType, AttributesType> {
//    if ('.' !in entityID)
//        throw IllegalArgumentException("entityID must be of type 'domain.name'")
//
//    val (domainName, name) = entityID.split('.')
//
//    return Entity(
//            kHomeAssistant = kHomeAssistant,
//            name = name,
//            domain = Domain(domainName)
//    )
//}

//fun KHomeAssistantContext.Entity(entityID: String) = Entity<Any, Attributes>(entityID = entityID)
