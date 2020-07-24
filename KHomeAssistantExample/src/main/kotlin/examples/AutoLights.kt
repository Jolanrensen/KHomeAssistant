package examples

import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Group
import nl.jolanrensen.kHomeAssistant.domains.input.InputBoolean
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.runEveryDayAtSunset

class AutoLights(kHass: KHomeAssistant) : Automation(kHass) {

    val allLights = Group["living_room_lights"]

    val autoLightsOn by InputBoolean["auto_living_room_lights_on"]
    val autoLightsOnSunset by InputBoolean["auto_living_room_lights_on_at_sunset"]
    val autoLightsOff by InputBoolean["auto_living_room_lights_off"]

    val turnOnTime = InputDatetime["living_room_lights_on"]
    val turnOffTime = InputDatetime["living_room_lights_off"]

    override suspend fun initialize() {
        listOf(
            allLights,
            autoLightsOn,
            autoLightsOnSunset,
            autoLightsOff,
            turnOnTime,
            turnOffTime
        ).forEach { println(it) }

        runEveryDayAtSunset {
            if (autoLightsOn && autoLightsOnSunset) {
                println("Sunset! turning on the lights.")
                allLights.turnOn()
            }
        }

        turnOnTime.runEveryDayAtTime {
            if (autoLightsOn && !autoLightsOnSunset) {
                println("Time to turn on the lights!")
                allLights.turnOn()
            }
        }

        turnOffTime.runEveryDayAtTime {
            if (autoLightsOff) {
                println("Time to turn off the lights!")
                allLights.turnOff()
            }
        }
    }

}