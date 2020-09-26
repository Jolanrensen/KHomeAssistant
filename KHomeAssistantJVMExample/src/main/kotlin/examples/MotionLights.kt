package examples

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.MotionBinarySensor
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity

/**
 * @param kHass the KHomeAssistant context to be passed to the super class (Automation).
 * @param sensor the motion sensor to trigger the automation
 * @param entityOn the entity to turn on for 60 seconds when the motion is detected. This can be any [ToggleEntity] like [Light] or [Switch].
 * @param delay the time for [entityOn] to be on when motion is detected by [sensor].
 */
class MotionLights(
    kHass: KHomeAssistant,
    val sensor: MotionBinarySensor.Entity,
    val entityOn: ToggleEntity<*>,
    val delay: TimeSpan = 60.seconds
) : Automation(kHass) {

    var task: Task? = null

    override suspend fun initialize() {
        sensor.onMotionDetected {
            if (sun.isDown) {
                entityOn.turnOn()

                task?.cancel()
                task = runIn(delay) { entityOn.turnOff() }
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