package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.helper.GeoPoint

object HomeAssistant : Domain<DefaultEntity> {
    override val domainName: String = "homeassistant"
    override var kHomeAssistant: () -> KHomeAssistant? = { null }

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Example.' from a KHomeAssistantContext instead of using ExampleDomain directly.""".trimMargin()
    }

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
        data = mapOf(
            "latitude" to JsonPrimitive(latitude),
            "longitude" to JsonPrimitive(longitude)
        )
    )

    /** Update the location of the Home Assistant default zone (usually “Home”). */
    suspend fun setLocation(location: GeoPoint) = setLocation(location.latitude, location.longitude)

    /** The HomeAssistant domain does not have an entity. */
    override fun Entity(name: String) = throw DomainHasNoEntityException()
}

/** Access your domain, and set the context correctly */
typealias HomeAssistantDomain = HomeAssistant

val KHomeAssistantContext.HomeAssistant: HomeAssistantDomain
    get() = HomeAssistantDomain.also { it.kHomeAssistant = kHomeAssistant }