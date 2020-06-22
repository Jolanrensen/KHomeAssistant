import com.soywiz.klock.minutes
import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import examples.Battery
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
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

class TestAutomation : Automation() {

    val denon_avrx2200w by MediaPlayer
    val bothDateAndTime = InputDatetime["both_date_and_time"]
    val onlyDate = InputDatetime["only_date"]
    val onlyTime = InputDatetime.Entity("only_time")

    override suspend fun initialize() {
        println(Domain("sensor")["pixel_2_xl_battery_level"])

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
    automations = listOf(TestAutomation(), Battery(20, true))
)

fun main() = runBlocking {
    println("running!")
    kHomeAssistant.run()
}

