package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.LightEntity

object Light : Domain {
    override val domainName = "light"

    override fun createEntity(context: WithKHomeAssistant, name: String) = context.LightEntity(name = name)
}


