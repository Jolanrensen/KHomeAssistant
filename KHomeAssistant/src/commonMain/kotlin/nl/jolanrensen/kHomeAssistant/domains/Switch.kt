package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.LightEntity
import nl.jolanrensen.kHomeAssistant.entities.SwitchEntity

object Switch : Domain {
    override val domainName = "switch"

    override fun createEntity(context: WithKHomeAssistant, name: String) = context.SwitchEntity(name = name)

}