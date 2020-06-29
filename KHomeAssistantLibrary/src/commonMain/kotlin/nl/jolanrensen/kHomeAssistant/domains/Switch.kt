package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.HassAttributes
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity


/**
 *
 * https://www.home-assistant.io/integrations/switch/
 * */
class Switch(override val kHassInstance: KHomeAssistant) : Domain<Switch.Entity> {
    override val domainName = "switch"

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?): Boolean = other is Switch
    override fun hashCode(): Int = domainName.hashCode()

    /** Constructor of Switch.Entity with right context */
    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity<HassAttributes>(
        kHassInstance = kHassInstance,
        domain = Switch(kHassInstance),
        name = name
    )
}

/** Access the Switch Domain. */
val KHomeAssistant.Switch: Switch
    get() = Switch(this)