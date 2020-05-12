package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity


/**
 *
 * https://www.home-assistant.io/integrations/switch/
 * */
class Switch(override var kHomeAssistant: () -> KHomeAssistant?) : Domain<Switch.Entity> {
    override val domainName = "switch"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Switch.' from a KHomeAssistantContext instead of using SwitchDomain directly.""".trimMargin()
    }

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?) = other is Switch
    override fun hashCode(): Int = domainName.hashCode()

    /** Constructor of Switch.Entity with right context */
    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity(
        kHomeAssistant = kHomeAssistant,
        domain = Switch(kHomeAssistant),
        name = name
    )
}

/** Access the Switch Domain. */
val KHomeAssistantContext.Switch: Switch
    get() = Switch(kHomeAssistant)