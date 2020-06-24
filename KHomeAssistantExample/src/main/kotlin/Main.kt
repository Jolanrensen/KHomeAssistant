import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.SceneEntityState
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn


class BedroomLights(kHass: KHomeAssistant) : Automation(kHass) {


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

class TestAutomation(kHass: KHomeAssistant) : Automation(kHass) {

    val denon_avrx2200w by MediaPlayer
    val bothDateAndTime = InputDatetime["both_date_and_time"]
    val onlyDate = InputDatetime["only_date"]
    val onlyTime = InputDatetime.Entity("only_time")

    val shield_cast by MediaPlayer

    val test by Domain("scene")
    val test2 by Domain("scene")

    override suspend fun initialize() {


        println(denon_avrx2200w)


//        println(
//            Scene.apply(
//                SceneEntityState(
//                    entity = Domain("light")["wall_lamp"],
//                    state = "off"
//                )
//            )
//        )

//        println(test)
//        println(test2)

//        val data = listOf(
//            SceneEntityState(
//                entity = denon_avrx2200w,
//                state = OnOff.ON,
//                attributes = json {
//                    "volume" to .25f
//                    "sound_mode" to "STEREO"
//                }
//            ),
//            SceneEntityState(
//                entity = Light["batik"],
//                state = OnOff.ON,
//                attributes = json {
//                    "brightness" to 1f
//                }
//            )
//        )
//
//        println(Scene.apply(data = data))


//        println(Domain("sensor")["pixel_2_xl_battery_level"])
//
//        Notify.notify(
//            serviceName = "mobile_app_pixel_2_xl",
//            message = "Kiekeboe",
//            title = "tooo"
//        )

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

val kHomeAssistant = KHomeAssistantInstance(
    host = "home.jolanrensen.nl",
    port = 8123,
    secure = true,
    debug = true,
    accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI"
)


fun main() = runBlocking {
    println("running!")
    kHomeAssistant.run(
        TestAutomation(kHomeAssistant)
    )
}

