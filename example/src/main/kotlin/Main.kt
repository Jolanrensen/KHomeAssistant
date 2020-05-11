import com.soywiz.klock.Month
import com.soywiz.klock.Year
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.*


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
                    val bothDateAndTime = InputDatetime["both_date_and_time"]
                    val onlyDate = InputDatetime["only_date"]
                    val onlyTime = InputDatetime["input_time"]

//                    InputText["text1"] {
//                        onAttributesChanged {
//                            println("text1 attributes changed! $rawAttributes")
//                        }
//                        onStateChanged {
//                            println("text1 state changed! $state")
//                        }
//
//                        for (i in 0..5)
//                            state = "test$i"
//                    }


                    bothDateAndTime {
                        onAttributesChanged {
                            println("bothDateAndTime attributes changed to $rawAttributes")
                        }
                        onStateChanged {
                            println("bothDateAndTime state changed to $state")
                        }

//                        setDateTime(
//                            DateTime(2020, 3, 23, 12, 9, 8).localUnadjusted
//                        )

                        year = Year(2021)
                        month = Month.November
                        day = 22

                        hour = 7
                        minute = 29
                        second = 4

                        println(this)
                    }

//                    Light["wall_lamp"] {
//                        onStateChanged {
//                            println("lamp is now $state")
//                        }
//
//                        turnOff()
//                        turnOn()
//                        turnOff()
//                        turnOn()
//                    }

                }
            )
        ).run()
    }

}