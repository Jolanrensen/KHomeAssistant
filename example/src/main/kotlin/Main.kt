
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
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
//                    var i = 0
//                    runEvery(5.seconds) {
//                        println("a: ${++i * 5} seconds have passed!")
//                    }
//
//                    var j = 0
//                    runEvery(7.seconds) {
//                        println("b: ${++j * 7} seconds have passed!")
//                    }

//                    var l = 0
//                    runEverySecond {
//                        println("c: ${++l} seconds have passed!")
//                    }
//                    runEverySecond {
//                        println("d: $l seconds have passed")
//                    }

//                    runEveryDayAtSunrise {
//                        ""
//                    }
//
//                    runEveryMinute {
//                        ""
//                    }

//                    var k = 0
//                    runAt(
//                        getNextExecutionTime = { DateTime.now().local + (++k).seconds },
//                        whenToUpdate = {}
//                    ) {
//                        println("IRREGULAR! waited for $k seconds")
//                    }


                }
            )
        ).run()
    }

}