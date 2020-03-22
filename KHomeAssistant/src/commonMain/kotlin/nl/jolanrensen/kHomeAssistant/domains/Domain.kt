package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.Entity


interface Domain : KHomeAssistantContext {
    val domainName: String

    /** Helper function to create an Entity in a domain, alternative to YourDomainEntity("name") */
    /** TODO not sure about this one yet */
    fun Entity(name: String): Entity<*,*>
}

/** Create a temporary Domain. Useful for quick service calls. */
fun <E : Entity<*, *>> KHomeAssistantContext.DomainWithEntity(domainName: String, invoke: KHomeAssistantContext.(name: String) -> E) = object : Domain {
    override val domainName = domainName
    override val kHomeAssistant: KHomeAssistant = this@DomainWithEntity.kHomeAssistant
    override fun Entity(name: String) = invoke(name)
}

/** Create a temporary Domain. Useful for quick service calls. */
fun KHomeAssistantContext.Domain(domainName: String) = object : Domain {
    override val domainName = domainName
    override val kHomeAssistant: KHomeAssistant = this@Domain.kHomeAssistant
    override fun Entity(name: String) = throw Exception("This is a temporary domain and thus has no associated Entity.")
}


/** Call a service with an entity and data
 * For instance, turning on a light would be
 * Light.callService("turn_on", Light.Entity("kitchen"), mapOf("brightness" to 100))
 *
 * */
suspend fun <D : Domain> D.callService(service: String, entity: Entity<*, *>? = null, data: Map<String, Any?>? = null): Unit = TODO("merge D in Entity maybe, also execute in kHomeAssistant")

/**
 * Light["turn_on"](Light.Entity("kitchen"), mapOf("brightness" to 100))
 * suspend not working
 * */
//suspend operator fun <D: Domain> D.get(service: String): (entity: Entity<*,*>?, data: Map<String, Any?>?) -> Unit = { entity, data ->
//    TODO("?")
//
//
//}