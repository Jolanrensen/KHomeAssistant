import com.soywiz.klock.minutes
import com.soywiz.klock.seconds
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.MotionBinarySensor
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChanged
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn
import nl.jolanrensen.kHomeAssistant.runAt
import nl.jolanrensen.kHomeAssistant.runEveryDayAtSunrise
import nl.jolanrensen.kHomeAssistant.runIn


class BedroomLights : Automation() {


//    val bed = Light.Entity("bed")
//    val bedroomLamp = Light.Entity("bedroom_lamp")
//    val globe = Light.Entity("globe")
//    val pisa = Light.Entity("pisa")

    override suspend fun initialize() {
        val (bed, bedroomLamp, globe, pisa) = Light["bed", "bedroom_lamp", "globe", "pisa"]
        val allLights = listOf(bed, bedroomLamp, globe, pisa)

        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn })
                allLights.turnOff()
            else
                allLights.turnOn()

            this.turnOff()
        }
    }
}

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

class MotionLights : Automation() {

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

class TestAutomation : Automation() {

    val denon_avrx2200w by MediaPlayer
    val bothDateAndTime = InputDatetime["both_date_and_time"]
    val onlyDate = InputDatetime["only_date"]
    val onlyTime = InputDatetime.Entity("only_time")

    override suspend fun initialize() {


        Notify.notify(
            serviceName = "mobile_app_pixel_2_xl",
            message = "Kiekeboe",
            title = "tooo"
        )

//        Mqtt.publish(
//            topic = "cmnd/sonoff1/POWER",
//            payload = "toggle",
//            qos = 1,
//            retain = true
//        )

//        println(denon_avrx2200w)
//        println(bothDateAndTime)
//        println(onlyDate)
//        println(onlyTime)
//
//        onEventFired("call_service") {
//            println("call service called!")
//            println(it)
//        }
//
//        onlyTime {
//            time += 1.minutes
//
//            println(this)
//
//            onStateChanged {  }
//        }

    }
}


//class MotionLights : Automation() {
//
//    override suspend fun initialize(): {
//        Sensor
//    }
//}

val kHomeAssistant = KHomeAssistant(
    host = "home.jolanrensen.nl",
    port = 8123,
    secure = true,
    debug = false,
    accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
    automations = listOf(TestAutomation())
)

fun main() = runBlocking {
    println("running!")
    kHomeAssistant.run()
}

