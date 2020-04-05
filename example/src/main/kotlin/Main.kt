import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.onAttributesChanged
import nl.jolanrensen.kHomeAssistant.entities.onStateChanged
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import java.lang.Exception
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
            debug = false,
            useCache = true,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                automation("1") {
                    launch {
                        launch {
                            throw Exception("automation crash")
                        }

                    }
                },
                automation("2") {
                    Light["bedaa"]
                        .onStateChanged {
                            println("state changed to: $it")
                        }.onAttributesChanged {
                            println("attributes changed to $it")
                        }
                }
            )
        ).run()
    }


}