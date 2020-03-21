package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Entity


interface Domain {
    val domainName: String

    /** Helper function to create an Entity in a domain, alternative to YourDomainEntity("name") */
    /** TODO not sure about this one yet */
    fun createEntity(context: WithKHomeAssistant, name: String): Entity<*,*>
}

/** Create a temporary Domain. Useful for quick service calls. */
fun <E : Entity<*, *>> DomainWithEntity(domainName: String, invoke: (context: WithKHomeAssistant, name: String) -> E) = object : Domain {
    override val domainName = domainName

    override fun createEntity(context: WithKHomeAssistant, name: String) = invoke(context, name)
}

/** Create a temporary Domain. Useful for quick service calls. */
fun Domain(domainName: String) = object : Domain {
    override val domainName = domainName

    override fun createEntity(context: WithKHomeAssistant, name: String) = throw Exception("This is a temporary domain and thus has no associated Entity.")
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