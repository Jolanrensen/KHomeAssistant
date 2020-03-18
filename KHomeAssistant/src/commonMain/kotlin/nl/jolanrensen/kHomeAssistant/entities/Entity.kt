package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.attributes.Attributes

open class Entity<StateType : Any, AttributesType : Attributes>(
        override val kHomeAssistant: KHomeAssistant,
        val domain: String,
        open val name: String
) : WithKHomeAssistant {


    /** Given a string stateValue, this method should return the correct StateType */
    open fun parseStateValue(stateValue: String): StateType? = null

    /** This method returns the state for this entity in the original String format */
    open fun getStateValue(state: StateType): String? = null


    suspend fun getState(): StateType? = kHomeAssistant.getState(this)
    suspend fun setState(s: StateType): Unit = TODO()

    suspend fun getAttributes(): AttributesType = TODO()


    constructor(kHomeAssistant: KHomeAssistant, entityID: String) : this(
            kHomeAssistant = kHomeAssistant,
            domain = entityID.split('.').first(),
            name = entityID.split('.').last()
    ) {
        if ('.' !in entityID)
            throw IllegalArgumentException("entityID must be of type 'domain.name'")
    }

    val entityID: String
        get() = "$domain.$name"

}

/**
 * All entities can be created without KHomeAssistant instance from within an Automation
 * and other classes having an instance as kHomeAssistant can be accessed through there anyways.
 * */
inline fun <reified StateType : Any, reified AttributesType : Attributes> WithKHomeAssistant.Entity(domain: String, name: String) = Entity<StateType, AttributesType>(
        kHomeAssistant = kHomeAssistant,
        domain = domain,
        name = name
)

inline fun <reified StateType : Any, reified AttributesType : Attributes> WithKHomeAssistant.Entity(entityID: String) = Entity<StateType, AttributesType>(
        kHomeAssistant = kHomeAssistant,
        entityID = entityID
)
