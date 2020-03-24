package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.Attributes
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.entities.Entity


interface Domain<E: Entity<out Any, out Attributes>> : KHomeAssistantContext {
    val domainName: String

    /** Helper function to create an Entity in a domain, alternative to YourDomainEntity("name") */
    fun Entity(name: String): E

    /** Helper function to check whether the context is present */
    fun checkContext()
}

class DomainHasNoEntityException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

/** Create a temporary Domain. Useful for quick service calls. */
//fun <E : Entity<*, *>> KHomeAssistantContext.DomainWithEntity(domainName: String, invoke: KHomeAssistantContext.(name: String) -> E) = object : Domain {
//    override val domainName = domainName
//    override val kHomeAssistant: () -> KHomeAssistant? = this@DomainWithEntity.kHomeAssistant
//    override fun Entity(name: String) = invoke(name)
//    override fun checkContext() = Unit // context is always present
//}

/** Create a temporary Domain. Useful for quick service calls. */
fun KHomeAssistantContext.Domain(domainName: String) = object : Domain<DefaultEntity> {
    override val domainName = domainName
    override val kHomeAssistant = this@Domain.kHomeAssistant
    override fun Entity(name: String): Entity<String, BaseAttributes> = object : Entity<String, BaseAttributes>(kHomeAssistant = kHomeAssistant, name = name, domain = this) {
        override val attributesSerializer = BaseAttributes.serializer()
        override fun parseStateValue(stateValue: String) = stateValue
        override fun getStateValue(state: String) = state
    }

    override fun checkContext() = Unit // context is always present
}


/** Call a service with an entity and data
 * For instance, turning on a light would be
 * Light.callService("turn_on", Light.Entity("kitchen"), mapOf("brightness" to 100))
 *
 * */
suspend fun <E: Entity<*, *>, D : Domain<E>> D.callService(service: String, entity: E? = null, data: Map<String, Any?>? = null): Unit = TODO("merge D in Entity maybe, also execute in kHomeAssistant")

/**
 * Light["turn_on"](Light.Entity("kitchen"), mapOf("brightness" to 100))
 * suspend not working
 * */
//suspend operator fun <D: Domain> D.get(service: String): (entity: Entity<*,*>?, data: Map<String, Any?>?) -> Unit = { entity, data ->
//    TODO("?")
//
//
//}