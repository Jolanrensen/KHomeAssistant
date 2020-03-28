import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
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
suspend fun main() {

//    val test = Entity()
    println("running!")

    val test: TimeSpan = 5.seconds


    val kHomeAssistant = KHomeAssistant(
        host = "home.jolanrensen.nl",
        port = 8123,
        secure = true,
        debug = true,
        justExecute = true,
        accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI"
    ) {
        val allAutomations = hashSetOf<Job>()
        for (i in 0..10) launch {
            Light.Entity("dream_world").toggle()
        }.also { allAutomations += it }

        allAutomations.joinAll()
    }.run()

}