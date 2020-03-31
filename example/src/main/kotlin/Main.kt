import kotlinx.coroutines.delay
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource


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
suspend fun main() {


//    val test = Entity()
    println("running!")
    KHomeAssistant(
        host = "home.jolanrensen.nl",
        port = 8123,
        secure = true,
        debug = true,
        justExecute = false,
        useCache = true,
        accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI"
    ) {
//        listOf("piano", "wall_lamp", "batik", "dream_world")
//            .map { Light.Entity(it) }
//            .forEach { it.toggle() }


        while (true) {
            delay(1000)
            println("batik = ${Light["batik"].getState()}")
        }

    }.run()

}