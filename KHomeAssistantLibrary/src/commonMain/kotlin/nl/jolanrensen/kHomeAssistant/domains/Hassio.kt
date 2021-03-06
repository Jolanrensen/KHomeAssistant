package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import nl.jolanrensen.kHomeAssistant.KHomeAssistant

/**
 *
 */
class Hassio(override val kHassInstance: KHomeAssistant) : Domain<Nothing> {
    override val domainName = "hassio"

    /** Making sure Hassio acts as a singleton. */
    override fun equals(other: Any?) = other is Hassio
    override fun hashCode(): Int = domainName.hashCode()

    /**
     * Restart a Hass.io docker add-on.
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnRestart(addOn: String) = callService(
        serviceName = "addon_restart",
        data = buildJsonObject { put("addon", addOn) }
    )

    /**
     * Start a Hass.io docker add-on.
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnStart(addOn: String) = callService(
        serviceName = "addon_start",
        data = buildJsonObject { put("addon", addOn) }
    )

    /**
     * Write data to a Hass.io docker add-on stdin .
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnStdin(addOn: String) = callService(
        serviceName = "addon_stdin",
        data = buildJsonObject { put("addon", addOn) }
    )

    /**
     * Stop a Hass.io docker add-on.
     * @param addOn The add-on slug. Example: "core_ssh".
     */
    suspend fun addOnStop(addOn: String) = callService(
        serviceName = "addon_stop",
        data = buildJsonObject { put("addon", addOn) }
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


    override fun Entity(name: String): Nothing = throw DomainHasNoEntityException()
}

/** Access the Hassio Domain. */
val KHomeAssistant.Hassio: Hassio
    get() = Hassio(this)