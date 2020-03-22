package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext

object HassioDomain : Domain {
    override val domainName = "hassio"

    override lateinit var kHomeAssistant: KHomeAssistant

    override fun checkContext() = require(::kHomeAssistant.isInitialized) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Hassio.' from a KHomeAssistantContext instead of using HassioDomain directly.""".trimMargin()
    }

    suspend fun addOnRestart(addOn: String) {
        checkContext()
        callService(service = "addon_restart", data = mapOf("addon" to addOn))
    }

    suspend fun addOnStart(addOn: String) {
        checkContext()
        callService(service = "addon_start", data = mapOf("addon" to addOn))
    }
}

val KHomeAssistantContext.Hassio get() = HassioDomain.also { it.kHomeAssistant = kHomeAssistant }