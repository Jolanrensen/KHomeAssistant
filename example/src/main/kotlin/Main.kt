import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.helper.RGBColor


class Test : Automation() {

    val bedroomLamp = Light.Entity("bedroom_lamp")
    val bed = Light.Entity("bed")
    val globe = Light.Entity("globe")

    val allLights = listOf(bedroomLamp, bed, globe)

    override suspend fun initialize() {
        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn })
                allLights.forEach { it.turnOff() }
            else
                allLights.forEach { it.turnOn() }

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
            debug = true,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                automation("1") {
                    val batik = Light.Entity("batik").onTurnOn { }


                    batik {
//                        turnOff()
//                        turnOnWithData(brightness = 2)
//                        println(brightness)
                        while (true) {
                            white_value = white_value!! + 1
                            if (white_value!! > 240) white_value = 0
                        }
                        rgb_color = RGBColor(255, 0, 0)
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