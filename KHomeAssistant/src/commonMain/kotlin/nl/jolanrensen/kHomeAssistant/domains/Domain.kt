package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonElement
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage


interface Domain<E: BaseEntity<out Any>> {
    val domainName: String

    var kHomeAssistant: () -> KHomeAssistant?

    /** Function to create an Entity in a domain */
    fun Entity(name: String): E

    /** Helper function to create multiple entities at once in a domain */
    fun Entities(vararg names: String): List<E> = names.map { Entity(it) }

    // TODO maybe allow a way to make an anonymous toggle entity

    /** Type YourDomain["entity"] instead of YourDomain.Entity("entity") */
    operator fun get(name: String): E = Entity(name)

    /** Type YourDomain["entity", "other_entity"] instead of YourDomain.Entities("entity", "other_entity") */
    operator fun get(name: String, vararg names: String): List<E> = Entities(name, *names)

    /** Helper function to check whether the context is present */
    fun checkContext()


    /** Call a service with an entity and data
     * For instance, turning on a light would be
     * Light.callService("turn_on", Light.Entity("kitchen"), mapOf("brightness" to 100))
     * */
    suspend fun callService(serviceName: String, data: Map<String, JsonElement> = mapOf()): ResultMessage {
        checkContext()
        return kHomeAssistant()!!.callService(
            domain = this,
            serviceName = serviceName,
            data = data
        )
    }

}

class DomainHasNoEntityException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}

/** Create a temporary Domain. Useful for quick service calls. */
fun KHomeAssistantContext.Domain(domainName: String) = object : Domain<DefaultEntity> {
    override val domainName = domainName
    override var kHomeAssistant = this@Domain.kHomeAssistant
    override fun Entity(name: String): DefaultEntity = object : DefaultEntity(kHomeAssistant = kHomeAssistant, name = name, domain = this) {
        override fun parseStateValue(stateValue: String) = stateValue
        override fun getStateValue(state: String) = state
    }

    override fun checkContext() = Unit // context is always present
}




/**
 * Light["turn_on"](Light.Entity("kitchen"), mapOf("brightness" to 100))
 * suspend not working
 * */
//suspend operator fun <D: Domain> D.get(service: String): (entity: Entity<*,*>?, data: Map<String, Any?>?) -> Unit = { entity, data ->
//    TODO("?")
//
//
//}