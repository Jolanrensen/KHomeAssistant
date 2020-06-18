package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity


/**
 *
 * https://www.home-assistant.io/integrations/switch/
 * */
class Switch(override var getKHomeAssistant: () -> KHomeAssistant?) : Domain<Switch.Entity> {
    override val domainName = "switch"

    override fun checkContext() = require(getKHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Switch.' from a KHomeAssistantContext instead of using SwitchDomain directly.""".trimMargin()
    }

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?) = other is Switch
    override fun hashCode(): Int = domainName.hashCode()

    /** Constructor of Switch.Entity with right context */
    override fun Entity(name: String) = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity(
        getKHomeAssistant = getKHomeAssistant,
        domain = Switch(getKHomeAssistant),
        name = name
    )
}

/** Access the Switch Domain. */
val HasContext.Switch: Switch
    get() = Switch(getKHomeAssistant)