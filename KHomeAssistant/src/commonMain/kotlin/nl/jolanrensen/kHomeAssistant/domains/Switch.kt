package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity

/** Do not use directly! Always use Switch.
 *
 * https://www.home-assistant.io/integrations/switch/
 * */
object Switch : Domain<Switch.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "switch"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Switch.' from a KHomeAssistantContext instead of using SwitchDomain directly.""".trimMargin()
    }

    /** Constructor of Switch.Entity with right context */
    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
            override val kHomeAssistant: () -> KHomeAssistant?,
            override val name: String
    ) : ToggleEntity<Entity.Attributes>(
            kHomeAssistant = kHomeAssistant,
            domain = Switch,
            name = name
    ) {
        @Serializable
        data class Attributes(
                override val friendly_name: String
        ) : BaseAttributes {
            override var fullJsonObject: JsonObject = JsonObject(mapOf())
        }

        override val attributesSerializer = Attributes.serializer()
    }
}

/** Access the SwitchDomain */
typealias SwitchDomain = Switch

val KHomeAssistantContext.Switch: SwitchDomain
    get() = SwitchDomain.also { it.kHomeAssistant = kHomeAssistant }