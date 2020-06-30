import com.soywiz.klock.minutes
import com.soywiz.korim.color.Colors
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.OnOff.*
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn


class BedroomLights(kHass: KHomeAssistant) : Automation(kHass) {

    val allLights = Light["bed", "bedroom_lamp", "globe", "pisa"]
    val bedroom_switch by Switch

    override suspend fun initialize() {
        bedroom_switch.onTurnOn {
            if (allLights.any { it.isOn }) {
                allLights.turnOff()
            } else {
                allLights.turnOn()
            }

            turnOff()
        }
    }
}

class TestAutomation(kHass: KHomeAssistant) : Automation(kHass) {

    val denon_avrx2200w by MediaPlayer
    val bothDateAndTime = InputDatetime["both_date_and_time"]
    val onlyDate = InputDatetime["only_date"]
    val onlyTime = InputDatetime.Entity("only_time")

    val shield_cast by MediaPlayer

    override suspend fun initialize() {
        Group["living_room_lights"].useAs(Light) {
            color = Colors.RED
            white_value = 100
        }
    }

}


//class MotionLights : Automation() {
//
//    override suspend fun initialize(): {
//        Sensor
//    }
//}

val kHomeAssistant = KHomeAssistantInstance(
    host = "home.jolanrensen.nl",
    port = 8123,
    secure = true,
    debug = true,
    accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI"
)


class OutsideLights(kHass: KHomeAssistant) : Automation(kHass) {

    private val offScene: Scene.Entity = Scene["outside_lights_off"]
    private val onScene: Scene.Entity = Scene["outside_lights_on"]

    override suspend fun initialize() {
        runEveryDayAtSunrise(15.minutes) {
            println("OutsideLights: Sunrise Triggered")
            offScene.turnOn()
        }
        runEveryDayAtSunset(-15.minutes) {
            println("OutsideLights: Sunset Triggered")
            onScene.turnOn()
        }

        callService(serviceDomain = "homeassistant", serviceName = "restart")
        HomeAssistant.restart()
        Domain("")[""].callService(serviceName = "", data = json { "some_value" to 10 })

        val lightState: OnOff = Light[""].state

when (lightState) {
    ON -> { /* do something */ }
    OFF -> { /* do something else */ }
    UNKNOWN, UNAVAILABLE -> { /* notify or something */ }
}

        callService(serviceDomain = "light", serviceName = "turn_on", entityID = "light.bedroom_lamp")
    }
}

fun main() = runBlocking {
    println("running!")
    kHomeAssistant.run(
        TestAutomation(kHomeAssistant)
    )
}

