package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.Entity

object Hassio : Domain {
    override val domainName = "hassio"
    override fun Entity(name: String): Entity<*, *> {
        TODO("Not yet implemented")
    }

    override val kHomeAssistant: KHomeAssistant
        get() = TODO("Not yet implemented")

    suspend fun addOnRestart(addOn: String) = callService(service = "addon_restart", data = mapOf("addon" to addOn))

    suspend fun addOnStart(addOn: String) = callService(service = "addon_start", data = mapOf("addon" to addOn))



}