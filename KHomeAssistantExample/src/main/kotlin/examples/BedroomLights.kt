package examples

import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.domains.getValue
import nl.jolanrensen.kHomeAssistant.entities.onTurnedOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn
import kotlin.math.abs

class BedroomLights(kHass: KHomeAssistant) : Automation(kHass) {

    private val bedroom_lamp by Light
    private val globe by Light
    private val pisa by Light
    private val bed by Light
    private val allLights = listOf(bedroom_lamp, globe, pisa, bed)

    private val bedroom_switch by Switch

    private val Light.Entity.isWarm: Boolean
        get() {
            val currentTemp = if (isOn) color_temp else max_mireds
            return abs(currentTemp - max_mireds) < abs(currentTemp - min_mireds)
        }

    private fun Light.Entity.toggleWarmth() {
        color_temp = if (isWarm) {
            println("making light cool")
            min_mireds
        } else {
            println("making light warm")
            max_mireds
        }
    }

    private suspend fun turnOffAllLights() {
        println("Turning off all lights")
        allLights.turnOff()
    }

    private suspend fun turnOnAllLights() {
        println("Turning on all lights")
        allLights.turnOn()

        // I don't really care when it doesn't succeed, so async is true
        bed.turnOn(
            async = true,
            brightness = 255,
            white_value = 120,
            effect = "colorloop"
        )

//        bed {
//            // bed light is often disconnected
//            try {
//                brightness = 255
//                white_value = 120
//                effect = "colorloop"
//            } catch (e: Exception) {
//                println(e)
//            }
//        }
    }

    private suspend fun toggleLights() = if (allLights.any { it.isOn }) turnOffAllLights() else turnOnAllLights()

    override suspend fun initialize() {
        bedroom_switch.onTurnedOn {
            println("bedroom switch turned on")
            if (allLights.all { isOff }) {
                toggleLights()
            } else {
                delay(0.5.seconds)

                if (isOn) toggleLights()
                else bedroom_lamp.toggleWarmth()
            }
            turnOff()
        }
    }
}
