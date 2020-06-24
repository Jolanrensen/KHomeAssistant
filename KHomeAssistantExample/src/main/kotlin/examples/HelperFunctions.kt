package examples

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Light

object HelperFunctions {

    /**
     * You can define helper functions simply by creating an extension function on [KHomeAssistant].
     * Now you can access entities, services etc and it will work in all automations.
     */
    fun KHomeAssistant.getAllLights(): List<Light.Entity> = entities
        .filter { it.domainName == "light" }
        .map { Light[it.name] }
}