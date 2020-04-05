import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
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


@OptIn(ExperimentalTime::class)
fun main() {
    runBlocking {
        println("running!")
        KHomeAssistant(
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
                    Domain("media_player")["denon_avrx2200w"].apply {
                        callService("turn_off")

//                        callService(
//                            "volume_set",
//                            mapOf("volume_level" to JsonPrimitive(0.25))
//                        )
                    }
                }
            )
        ).run()
    }


}