package examples

import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.entities.turnOff

class TurnOffAllLightsAt(kHass: KHomeAssistant, private val time: TimeOfDay) : Automation(kHass) {

    override suspend fun initialize() {
        runEveryDayAt(time) {
            entities  // get all entities
                .filter { it.domainName == Light.domainName }  // filter to get only the lights
                .map { Light[it.name] }  // map them to light entities to be able to easily control them
                .turnOff()  // turn them all off at once
        }
    }
}

// functional variant
fun turnOffAllLightsAt(kHass: KHomeAssistant, time: TimeOfDay): Automation = automation(kHass, "TurnOffAllLightsAt") {
    runEveryDayAt(time) {
        entities  // get all entities
            .filter { it.domainName == Light.domainName }  // filter to get only the lights
            .map { Light[it.name] }  // map them to light entities to be able to easily control them
            .turnOff()  // turn them all off at once
    }
}