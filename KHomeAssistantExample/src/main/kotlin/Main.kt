import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.minutes
import com.soywiz.klock.seconds
import examples.BedroomLights
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.runAt
import nl.jolanrensen.kHomeAssistant.runEverySecond


class TestAutomation(kHass: KHomeAssistant) : Automation(kHass) {

//    val denon_avrx2200w by MediaPlayer
//    val bothDateAndTime = InputDatetime["both_date_and_time"]
//    val onlyDate = InputDatetime["only_date"]
//    val onlyTime = InputDatetime.Entity("only_time")
//
//    val shield_cast by MediaPlayer
//
//    val bedroom_lamp by Light
//
//    val oosterhout by Weather
//
//    val lock_linux by AlarmControlPanelNumber

    override suspend fun initialize() {

//        val test = AbstractBinarySensor.BinarySensorDeviceClass.GENERIC.domain(this)["test"]

//        println(lock_linux)

        runEverySecond {
            println("running every second!")
        }

        runAt(
            getNextLocalExecutionTime = { DateTimeTz.nowLocal() + 10.seconds },
            whenToUpdate = {
                runEverySecond(it)
            }
        ) {
            println("running whenever")
        }
//        Group["living_room_lights"].useAs(Light) {
//            color = Colors.RED
//            white_value = 100
//        }
    }
}

fun main() = runBlocking {
    println("running!")
    kHomeAssistant.run(
        TestAutomation(kHomeAssistant)
//        BedroomLights(kHomeAssistant)
//        AutoLights(kHomeAssistant)
    )
}

