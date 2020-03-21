package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant

object Hassio : Domain<Nothing> {
    override val domainName = "hassio"

    suspend fun addOnRestart(addOn: String) = callService(service = "addon_restart", data = mapOf("addon" to addOn))

    suspend fun addOnStart(addOn: String) = callService(service = "addon_start", data = mapOf("addon" to addOn))

    override fun invoke(context: WithKHomeAssistant, name: String): Nothing {
        TODO("Not implemented as Hassio does not have any Entities")
    }

}