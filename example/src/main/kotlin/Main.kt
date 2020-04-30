
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn


class BedroomLights : Automation() {

    val bed = Light.Entity("bed")
    val bedroomLamp = Light.Entity("bedroom_lamp")
    val globe = Light.Entity("globe")
    val pisa = Light.Entity("pisa")

    val allLights = listOf(bed, bedroomLamp, globe, pisa)

    override suspend fun initialize() {
        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn })
                allLights.turnOff()
            else
                allLights.turnOn()

            this.turnOff()
        }
    }
}

//fun main() {
//    val queue = priorityQueueOf(9, 8, 5, 6)
//    queue.push(7)
//    println(queue.heap.toList())
//
//    queue.remove(9)
//    println(queue.heap.toList())
//
//    for (item in queue) {
//        println(queue.extractNext())
//        println(queue.heap.toList())
//    }
//}

fun main() {
    runBlocking {
        println("running!")

        KHomeAssistant(
            host = "home.jolanrensen.nl",
            port = 8123,
            secure = true,
            debug = true,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                automation("1") {
                    val dreamWorld = Light["dream_world"]

                    var timesTurnedOn = 0
                    var timesTurnedOff = 0

                    (1..20).forEach {
                        if (dreamWorld.isOn) {
                            dreamWorld.turnOff()
                            timesTurnedOff++
                        } else {
                            dreamWorld.turnOn()
                            timesTurnedOn++
                        }
                    }

                    println("$timesTurnedOn times turned on and $timesTurnedOff times turned off")

                }
            )
        ).run()
    }

}