package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.states.State

open class Entity<S : State<*>>(
        override val kHomeAssistant: KHomeAssistant,
        val domain: String,
        open val name: String
) : WithKHomeAssistant {

    suspend fun getState(): S? = kHomeAssistant.getState(this)
    suspend fun setState(s: S): Unit = TODO()

//    var state: S?
//        get() = .await
//        set(value) {}

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
inline fun <reified S : State<*>> WithKHomeAssistant.Entity(domain: String, name: String) = Entity<S>(
        kHomeAssistant = kHomeAssistant,
        domain = domain,
        name = name
)

inline fun <reified S : State<*>> WithKHomeAssistant.Entity(entityID: String) = Entity<S>(
        kHomeAssistant = kHomeAssistant,
        entityID = entityID
)
