import com.soywiz.korim.color.Colors
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.*
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.AbstractBinarySensor
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.*


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

    override suspend fun initialize() {

//        val test = AbstractBinarySensor.BinarySensorDeviceClass.GENERIC.domain(this)["test"]

        println(denon_avrx2200w)


        oosterhout {
            println(this)
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

