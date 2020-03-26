package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.attributes.SerializableBaseAttributes
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.messages.Context

typealias DefaultEntity = BaseEntity<String, SerializableBaseAttributes>

open class BaseEntity<StateType : Any, AttributesType : BaseAttributes>(
        open val kHomeAssistant: () -> KHomeAssistant? = { null },
        open val name: String,
        open val domain: Domain<out BaseEntity<out StateType, out AttributesType>>
) {
    private var entityExists = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    fun checkEntityExists() {
        return // TODO
        if (entityExists) return
        kHomeAssistant?.invoke()?.coroutineScope?.launch {
            getState() // throws error if entity does not exist
            entityExists = true
        }

    }

    open val attributesSerializer: KSerializer<AttributesType>? = null

    init {
        checkEntityExists()
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


    suspend fun callService(serviceName: String, data: Map<String, JsonElement> = mapOf()) =
            kHomeAssistant()!!.callService(
                    entity = this,
                    serviceName = serviceName,
                    data = data
            )

    val entityID: String
        get() = "${domain.domainName}.$name"

}

fun <S : Any, A : BaseAttributes, E : BaseEntity<S, A>> E.onStateChange(condition: (newState: S?) -> Boolean, callback: suspend E.() -> Unit) =
        onStateChange { newState ->
            if (condition(newState))
                callback()
        }

fun <S : Any, A : BaseAttributes, E : BaseEntity<S, A>> E.onStateChange(callback: suspend E.(newState: S?) -> Unit) {
    checkEntityExists()
    kHomeAssistant()!!.stateListeners
            .getOrPut(entityID) { hashSetOf() }
            .add {
                callback(parseStateValue(it.state))
            }
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
