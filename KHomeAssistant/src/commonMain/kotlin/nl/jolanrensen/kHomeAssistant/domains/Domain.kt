package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonElement
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity


interface Domain<E: BaseEntity<out Any, out BaseAttributes>> {
    val domainName: String

    var kHomeAssistant: () -> KHomeAssistant?

    /** Helper function to create an Entity in a domain, alternative to YourDomainEntity("name") */
    fun Entity(name: String): E

    /** Helper function to check whether the context is present */
    fun checkContext()


    /** Call a service with an entity and data
     * For instance, turning on a light would be
     * Light.callService("turn_on", Light.Entity("kitchen"), mapOf("brightness" to 100))
     * */
    suspend fun callService(serviceName: String, data: Map<String, JsonElement> = mapOf()) =
            kHomeAssistant()!!.callService(
                    domain = this,
                    serviceName = serviceName,
                    data = data
            )

}

class DomainHasNoEntityException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

/** Create a temporary Domain. Useful for quick service calls. */
fun KHomeAssistantContext.Domain(domainName: String) = object : Domain<DefaultEntity> {
    override val domainName = domainName
    override var kHomeAssistant = this@Domain.kHomeAssistant
    override fun Entity(name: String): DefaultEntity = object : DefaultEntity(kHomeAssistant = kHomeAssistant, name = name, domain = this) {
        override val attributesSerializer = BaseAttributes.serializer()
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