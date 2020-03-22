package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.SwitchEntity

object Switch : Domain {
    override val domainName = "switch"

    override fun createEntity(context: KHomeAssistantContext, name: String) = context.SwitchEntity(name = name)

}