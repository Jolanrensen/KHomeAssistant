package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.helper.GeoPoint

/**
 * https://www.home-assistant.io/integrations/homeassistant
 */
class HomeAssistant(override val kHassInstance: KHomeAssistant) : Domain<Nothing> {
    override val domainName: String = "homeassistant"

    /** Making sure HomeAssistant acts as a singleton. */
    override fun equals(other: Any?) = other is HomeAssistant
    override fun hashCode(): Int = domainName.hashCode()

    /** Reads the configuration files and checks them for correctness, but does not load them into Home Assistant. Creates a persistent notification and log entry if errors are found. */
    suspend fun checkConfig() = callService("check_config")

    /** Loads the main configuration file (configuration.yaml) and all linked files. Once loaded the new configuration is applied. */
    suspend fun reloadCoreConfig() = callService("reload_core_config")

    /** Restarts the Home Assistant instance (also reloading the configuration on start). */
    suspend fun restart() = callService("restart")

    /** Stops the Home Assistant instance. Home Assistant must be restarted from the Host device to run again. */
    suspend fun stop() = callService("stop")

    /** Update the location of the Home Assistant default zone (usually “Home”). */
    suspend fun setLocation(latitude: Float, longitude: Float) = callService(
        serviceName = "set_location",
        data = json {
            "latitude" to latitude
            "longitude" to longitude
        }
    )

    /** Update the location of the Home Assistant default zone (usually “Home”). */
    suspend fun setLocation(location: GeoPoint) = setLocation(location.latitude, location.longitude)

    /** The HomeAssistant domain does not have an entity. */
    override fun Entity(name: String): Nothing = throw DomainHasNoEntityException()
}


/** Access the HomeAssistant Domain. */
val KHomeAssistant.HomeAssistant: HomeAssistant
    get() = HomeAssistant(this)