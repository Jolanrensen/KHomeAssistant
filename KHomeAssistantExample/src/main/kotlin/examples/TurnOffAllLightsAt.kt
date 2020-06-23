package examples

import com.soywiz.klock.Time
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.runEveryDayAt

class TurnOffAllLightsAt(private val time: Time) : Automation() {

    override suspend fun initialize() {
        runEveryDayAt(time) {
            kHassInstance!!
                .entities  // get all entities
                .filter { it.domainName == Light.domainName }  // filter to get only the lights
                .map { Light[it.name] }  // map them to light entities to be able to easily control them
                .turnOff()  // turn them all off at once
        }
    }
}

// functional variant
fun turnOffAllLightsAt(time: Time): Automation = automation("TurnOffAllLightsAt") {
    runEveryDayAt(time) {
        kHassInstance!!
            .entities  // get all entities
            .filter { it.domainName == Light.domainName }  // filter to get only the lights
            .map { Light[it.name] }  // map them to light entities to be able to easily control them
            .turnOff()  // turn them all off at once
    }
}