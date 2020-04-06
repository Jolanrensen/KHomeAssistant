import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.onStateChanged
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import kotlin.time.ExperimentalTime


class Test : Automation() {

    val bedroomLamp = Light.Entity("bedroom_lamp")
    val bed = Light.Entity("bed")
    val globe = Light.Entity("globe")

    val allLights = listOf(bedroomLamp, bed, globe)

    override suspend fun initialize() {
        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn() })
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


@OptIn(ExperimentalTime::class)
fun main() {
    runBlocking {
        println("running!")

        val instance = KHomeAssistant(
            host = "home.jolanrensen.nl",
            port = 8123,
            secure = true,
            debug = true,
            useCache = true,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
//                automation("1") {
//                    launch {
//                        launch {
//                            // tesdf
//                           println("mooie test")
//                            throw Exception("automation crash")
//                        }
//
//                    }
//                },
                automation("2") {
                    // added some sort of test
                    println("test1")
                    Light["wall_lamp"].onStateChanged {
                        println("newState: $it")
                    }
                }
            )
        )

        Instance.kHomeAssistant = instance
        instance.run()
    }

}