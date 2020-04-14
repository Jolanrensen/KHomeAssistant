import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.Time
import com.soywiz.klock.minutes
import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.RGBColor


class Test : Automation() {

    val bedroomLamp = Light.Entity("bedroom_lamp")
    val bed = Light.Entity("bed")
    val globe = Light.Entity("globe")

    val allLights = listOf(bedroomLamp, bed, globe)

    override suspend fun initialize() {
        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn })
                allLights.turnOff()
            else
                allLights.turnOn()

            turnOff()
        }
    }
}

object Instance {
    var kHomeAssistant: KHomeAssistant? = null
}


fun main() {
    runBlocking {
        println("running!")

        val instance = KHomeAssistant(
            host = "home.jolanrensen.nl",
            port = 8123,
            secure = true,
            debug = false,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                automation("1") {

                    println(DateTime.EPOCH.local)

                    val time = Time(hour = 17, second = 0, minute = 0)

//                    runEveryDayAt(hour = 17, second = 0, minute = 0) {
//                        println("hoi")
//                    }

                    Light["wall_lamp", "batik", "piano", "table_lamp", "dream_world"] {
                        println("supported features of $friendly_name is $supported_features")
                    }






                }
//                automation("2") {
//                    // added some sort of test
//                    println(DateTime(DateTime.EPOCH.date, Time(13)).localUnadjusted)
//                    Light["wall_lamp"].onStateChanged {
//                        println("newState: $it")
//                    }
//
//                    runEvery(1.seconds * 2) {
//                        println("1 second has passed! The time is ${DateTime.now().toString(DateFormat("EEE, dd MMM yyyy HH:mm:ss::SSS z"))}")
//                    }
//
//                    runEvery(1.7.seconds) {
//                        println("1.7 seconds have passed! The time is ${DateTime.now().toString(DateFormat("EEE, dd MMM yyyy HH:mm:ss::SSS z"))}")
//                    }
//
//                    val twoSecondsPastLastMidnight = DateTime(
//                        date = DateTime.now().date,
//                        time = Time(hour = 0, second = 2)
//                    ).localUnadjusted
//                    runEvery(5.seconds, alignWith = twoSecondsPastLastMidnight) {
//                        println("5 seconds have passed! The time is ${DateTime.now().toString(DateFormat("EEE, dd MMM yyyy HH:mm:ss::SSS z"))}")
//                    }
//
//                }
            )
        )

        Instance.kHomeAssistant = instance
        instance.run()
    }

}