package examples

import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.MotionBinarySensor
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.runIn

class FlashyMotionLights : Automation() {

    private val livingRoomLight = Light["living_room"]
    private val driveLight = Light["drive"]
    private val driveSensor = MotionBinarySensor["drive"]

    override suspend fun initialize() {
        driveSensor.onMotionDetected {
            if (sun.isDown) {
                driveLight.turnOn()
                runIn(60.seconds) { driveLight.turnOff() }

                flashWarning()
            }
        }
    }

    private suspend fun flashWarning() {
        repeat(9) { // uneven number so the original state stays the same
            livingRoomLight.toggle()
            delay(1.seconds)
        }
    }
}
