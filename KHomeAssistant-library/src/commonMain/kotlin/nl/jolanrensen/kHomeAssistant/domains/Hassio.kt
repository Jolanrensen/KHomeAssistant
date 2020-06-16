package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity

/**
 *
 */
class Hassio(override var kHomeAssistant: () -> KHomeAssistant? ) : Domain<DefaultEntity> {
    override val domainName = "hassio"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Hassio.' from a KHomeAssistantContext instead of using HassioDomain directly.""".trimMargin()
    }

    /** Making sure Hassio acts as a singleton. */
    override fun equals(other: Any?) = other is Hassio
    override fun hashCode(): Int = domainName.hashCode()

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

/** Access the Hassio Domain. */
val KHomeAssistantContext.Hassio: Hassio
    get() = Hassio(kHomeAssistant)