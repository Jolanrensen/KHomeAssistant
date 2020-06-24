package examples

import com.soywiz.klock.minutes
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.runEveryDayAtSunrise
import nl.jolanrensen.kHomeAssistant.runEveryDayAtSunset

class OutsideLights(kHass: KHomeAssistant) : Automation(kHass) {

    // TODO add scenes
    val offScene = Domain("scene")["off_scene"]
    val onScene = Domain("scene")["on_scene"]

    override suspend fun initialize() {
        runEveryDayAtSunrise {
            // TODO
            offScene.callService("turn_on")
        }

        runEveryDayAtSunset(-15.minutes) {
            onScene.callService("turn_on")
        }

    }
}