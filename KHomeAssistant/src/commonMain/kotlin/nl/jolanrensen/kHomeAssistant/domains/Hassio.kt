package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.entities.Entity

object HassioDomain : Domain<DefaultEntity> {
    override val domainName = "hassio"

    override var kHomeAssistant: () -> KHomeAssistant? = { null }

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Hassio.' from a KHomeAssistantContext instead of using HassioDomain directly.""".trimMargin()
    }

    suspend fun addOnRestart(addOn: String) {
        checkContext()
        callService(serviceName = "addon_restart", data = mapOf("addon" to JsonPrimitive(addOn)))
    }

    suspend fun addOnStart(addOn: String) {
        checkContext()
        callService(serviceName = "addon_start", data = mapOf("addon" to JsonPrimitive(addOn)))
    }

    override fun Entity(name: String): Entity<String, BaseAttributes> = throw DomainHasNoEntityException()
}

val KHomeAssistantContext.Hassio get() = HassioDomain.also { it.kHomeAssistant = kHomeAssistant }