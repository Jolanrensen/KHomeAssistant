package examples

import com.soywiz.klock.minutes
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChanged
import nl.jolanrensen.kHomeAssistant.runAt
import nl.jolanrensen.kHomeAssistant.runEveryDayAtSunrise

class OutsideLights : Automation() {

    // TODO add scenes
    val offScene = Domain("scene")["off_scene"]
    val onScene = Domain("scene")["on_scene"]

    override suspend fun initialize() {
        runEveryDayAtSunrise {
            // TODO
            offScene.callService("turn_on")
        }

        // TODO maybe add an "offset" option to sunrise/-set etc
        runAt(
            getNextLocalExecutionTime = { sun.next_setting.local - 15.minutes },
            whenToUpdate = { update -> sun.onAttributeChanged(sun::next_setting) { update() } }
        ) {
            onScene.callService("turn_on")
        }
    }
}