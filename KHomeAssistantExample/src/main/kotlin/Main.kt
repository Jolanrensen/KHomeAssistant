import com.soywiz.klock.minutes
import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import kotlinx.serialization.json.floatOrNull
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.MotionBinarySensor
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.*
import java.util.*


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

class Battery(private val threshold: Float, private val alwaysSend: Boolean = false) : Automation() {

    override suspend fun initialize() {
        runEveryDayAt(hour = 6) {
            val batteryDevices: HashSet<Pair<DefaultEntity, Float>> = hashSetOf()

            for (device in kHomeAssistantInstance!!.entities) {
                (device["battery"] ?: device["battery_level"])
                    ?.floatOrNull
                    ?.let { battery ->
                        batteryDevices += device to battery
                    }
            }

            val lowDevices: List<Pair<DefaultEntity, Float>> = batteryDevices.filter { it.second < threshold }

            var message = "Battery Level Report\n\n"

            if (lowDevices.isNotEmpty()) {
                message += """The following devices are low:
                    |${lowDevices.joinToString("\n") { it.first.entityID }}
                    |""".trimMargin()
            }

            message += """Battery Levels:
                |${batteryDevices.joinToString("\n") { "${it.first.entityID}: ${it.second}" }}
                |""".trimMargin()

            if (lowDevices.isNotEmpty() || alwaysSend) {
                Notify.notify(title = "Home Assistant Battery Report", message = message)
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

