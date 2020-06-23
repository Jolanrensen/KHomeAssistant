package examples

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.domains.Light

object HelperFunctions {

    /**
     * You can define helper functions simply by creating an extension function on [HasKHassContext].
     * Now you can access entities, services etc and it will work in all automations.
     */
    fun HasKHassContext.getAllLights(): List<Light.Entity> = getKHass()!!
        .entities
        .filter { it.domainName == "light" }
        .map { Light[it.name] }
}