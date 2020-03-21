package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.attributes.Attributes
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.DomainWithEntity
import nl.jolanrensen.kHomeAssistant.messages.Context

open class Entity<StateType : Any, AttributesType : Attributes>(
        override val kHomeAssistant: KHomeAssistant,
        open val name: String,
        open val domain: Domain
) : WithKHomeAssistant {

    /** Given a string stateValue, this method should return the correct StateType */
    open fun parseStateValue(stateValue: String): StateType? = null

    /** This method returns the state for this entity in the original String format */
    open fun getStateValue(state: StateType): String? = null


    suspend fun getState(): StateType? = kHomeAssistant.getState(this)
    suspend fun setState(s: StateType): Unit = TODO()

    suspend fun getAttributes(): AttributesType = TODO()

    suspend fun getLastChanged(): String = TODO("last_changed uit State")
    suspend fun getLastUpdated(): String = TODO("last_updated uit State")
    suspend fun getContext(): Context = TODO("context uit State")


    val entityID: String
        get() = "${domain.domainName}.$name"

}

/**
 * All entities can be created without KHomeAssistant instance from within an Automation
 * and other classes having an instance as kHomeAssistant can be accessed through there anyways.
 * */
inline fun <reified StateType : Any, reified AttributesType : Attributes> WithKHomeAssistant.Entity(domain: Domain, name: String): Entity<StateType, AttributesType> =
        Entity(
                kHomeAssistant = kHomeAssistant,
                domain = domain,
                name = name
        )

fun WithKHomeAssistant.Entity(domain: Domain, name: String) =
        Entity<Any, Attributes>(
                domain = domain,
                name = name
        )


inline fun <reified StateType : Any, reified AttributesType : Attributes>
        WithKHomeAssistant.Entity(domainName: String, name: String): Entity<StateType, AttributesType> {
    var e: Entity<StateType, AttributesType>? = null
    e = Entity(
            kHomeAssistant = kHomeAssistant,
            name = name,
            domain = DomainWithEntity(domainName) { context, name ->
                e!! //tODO check if this works
            }
    )
    return e
}

fun WithKHomeAssistant.Entity(domainName: String, name: String) =
        Entity<Any, Attributes>(
                domainName = domainName,
                name = name
        )


inline fun <reified StateType : Any, reified AttributesType : Attributes> WithKHomeAssistant.Entity(entityID: String): Entity<StateType, AttributesType> {
    if ('.' !in entityID)
        throw IllegalArgumentException("entityID must be of type 'domain.name'")

    val (domainName, name) = entityID.split('.')

    return Entity(
            kHomeAssistant = kHomeAssistant,
            name = name,
            domain = Domain(domainName)
    )
}

fun WithKHomeAssistant.Entity(entityID: String) = Entity<Any, Attributes>(entityID = entityID)
