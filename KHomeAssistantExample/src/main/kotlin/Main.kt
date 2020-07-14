import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import kotlinx.coroutines.delay
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onTurnedOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn


class BedroomLights(kHass: KHomeAssistant) : Automation(kHass) {

    private val allLights: List<Light.Entity> = Light["bed", "bedroom_lamp", "globe", "pisa"]
    private val bedroom_switch by Switch

    override suspend fun initialize() {
        bedroom_switch.onTurnedOn {
            if (allLights.any { it.isOn }) {
                allLights.turnOff()
            } else {
                allLights.turnOn()
            }

            turnOff()
        }
    }
}

class TestAutomation(kHass: KHomeAssistant) : Automation(kHass) {

    val denon_avrx2200w by MediaPlayer
    val bothDateAndTime = InputDatetime["both_date_and_time"]
    val onlyDate = InputDatetime["only_date"]
    val onlyTime = InputDatetime.Entity("only_time")

    val shield_cast by MediaPlayer

    val bedroom_lamp by Light

    val oosterhout by Weather

    val lock_linux by AlarmControlPanelNumber

    override suspend fun initialize() {

//        val test = AbstractBinarySensor.BinarySensorDeviceClass.GENERIC.domain(this)["test"]

        println(lock_linux)

        lock_linux {
            armAway(2772)
            delay(5.seconds)
            disarm(2772)
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
    )
}

