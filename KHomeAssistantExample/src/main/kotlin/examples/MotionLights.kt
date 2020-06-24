package examples

import com.soywiz.klock.seconds
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.MotionBinarySensor
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.runIn

class MotionLights(kHass: KHomeAssistant) : Automation(kHass) {

    private val driveLight = Light["drive"]
    private val driveSensor = MotionBinarySensor["drive"]

    override suspend fun initialize() {
        driveSensor.onMotionDetected {
            if (sun.isDown) {
                driveLight.turnOn()
                runIn(60.seconds) { driveLight.turnOff() }
            }
        }
    }
}

// functional variant
fun motionLights(kHass: KHomeAssistant): Automation = automation(kHass, "MotionLights") {
    val driveLight = Light["drive"]
    val driveSensor = MotionBinarySensor["drive"]

    driveSensor.onMotionDetected {
        if (sun.isDown) {
            driveLight.turnOn()
            runIn(60.seconds) { driveLight.turnOff() }
        }
    }
}