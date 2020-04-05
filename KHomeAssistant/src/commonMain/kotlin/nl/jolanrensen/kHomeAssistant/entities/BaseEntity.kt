package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.attributes.DefaultAttributes
import nl.jolanrensen.kHomeAssistant.attributes.attributesFromJson
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.HomeAssistant
import nl.jolanrensen.kHomeAssistant.messages.Context
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

typealias DefaultEntity = BaseEntity<String, DefaultAttributes>

open class BaseEntity<StateType : Any, AttributesType : BaseAttributes>(
    open val kHomeAssistant: () -> KHomeAssistant? = { null },
    open val name: String,
    open val domain: Domain<out BaseEntity<out StateType, out AttributesType>>
) {
    private var entityExists = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    open fun checkEntityExists() {
        if (entityExists) return
        kHomeAssistant?.invoke()?.launch {
            getState() // throws error if entity does not exist
            entityExists = true
        }

    }

    open val attributesSerializer: KSerializer<AttributesType>? = null

    init {
        this.checkEntityExists()
    }

    /** Given a string stateValue, this method should return the correct StateType */
    open fun parseStateValue(stateValue: String): StateType? = null

    /** This method returns the state for this entity in the original String format */
    open fun getStateValue(state: StateType): String? = null

    suspend fun getState(): StateType = kHomeAssistant()!!.getState(this)
    suspend fun setState(s: StateType): Unit = TODO()

    suspend fun getAttributes(): AttributesType = kHomeAssistant()!!.getAttributes(this, attributesSerializer!!)

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

fun <S : Any, A : BaseAttributes, E : BaseEntity<S, A>> E.onStateChangedToNot(
    newState: S,
    callback: suspend E.() -> Unit
): E {
    onStateChanged { it ->
        if (newState != it)
            callback()
    }
    return this
}

fun <S : Any, A : BaseAttributes, E : BaseEntity<S, A>> E.onStateChangedTo(
    newState: S,
    callback: suspend E.() -> Unit
): E {
    onStateChanged { it ->
        if (newState == it)
            callback()
    }
    return this
}

fun <S : Any, A : BaseAttributes, E : BaseEntity<S, A>> E.onStateChanged(
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

fun <S : Any, A : BaseAttributes, E : BaseEntity<S, A>> E.onAttributesChanged(
    callback: suspend E.(newAttributes: A?) -> Unit
): E {
    checkEntityExists()
    kHomeAssistant()!!.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add { oldState, newState ->
            if (oldState.attributes != newState.attributes)
                callback(attributesFromJson(newState.attributes, attributesSerializer!!))
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
