package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity


/**
 *
 * https://www.home-assistant.io/integrations/switch/
 * */
class Switch(kHassInstance: KHomeAssistant) : Domain<Switch.Entity>, KHomeAssistant by kHassInstance {
    override val domainName = "switch"

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?) = other is Switch
    override fun hashCode(): Int = domainName.hashCode()

    /** Constructor of Switch.Entity with right context */
    override fun Entity(name: String) = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity(
        kHassInstance = kHassInstance,
        domain = Switch(kHassInstance),
        name = name
    )
}

/** Access the Switch Domain. */
val KHomeAssistant.Switch: Switch
    get() = Switch(this)