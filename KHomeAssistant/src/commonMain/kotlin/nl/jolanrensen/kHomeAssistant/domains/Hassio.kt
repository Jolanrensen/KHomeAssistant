package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity

object Hassio : Domain<DefaultEntity> {
    override val domainName = "hassio"

    override var kHomeAssistant: () -> KHomeAssistant? = { null }

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Hassio.' from a KHomeAssistantContext instead of using HassioDomain directly.""".trimMargin()
    }

    /**
     * Restart a Hass.io docker add-on.
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnRestart(addOn: String) = callService(
        serviceName = "addon_restart",
        data = mapOf("addon" to JsonPrimitive(addOn))
    )

    /**
     * Start a Hass.io docker add-on.
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnStart(addOn: String) = callService(
        serviceName = "addon_start",
        data = mapOf("addon" to JsonPrimitive(addOn))
    )

    /**
     * Write data to a Hass.io docker add-on stdin .
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnStdin(addOn: String) = callService(
        serviceName = "addon_stdin",
        data = mapOf("addon" to JsonPrimitive(addOn))
    )

    /**
     * Stop a Hass.io docker add-on.
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnStop(addOn: String) = callService(
        serviceName = "addon_stop",
        data = mapOf("addon" to JsonPrimitive(addOn))
    )

    /** Reboot the host system. */
    suspend fun hostReboot() = callService("host_reboot")

    /** Poweroff the host system. */
    suspend fun hostShutdown() = callService("host_shutdown")


    /** Restore full snapshot. */
    suspend fun restoreFull() = callService("restore_full")

    /** Restore partial snapshot. */
    suspend fun restorePartial() = callService("restore_partial")

    /** Create full snapshot. */
    suspend fun snapshotFull() = callService("snapshot_full")

    /** Create partial snapshot. */
    suspend fun snapshotParial() = callService("snapshot_partial")


    override fun Entity(name: String): DefaultEntity = throw DomainHasNoEntityException()
}

typealias HassioDomain = Hassio

val KHomeAssistantContext.Hassio: HassioDomain
    get() = HassioDomain.also { it.kHomeAssistant = kHomeAssistant }