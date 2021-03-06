package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.minutes
import com.soywiz.klock.plus
import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.domains.getValue
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.HassAttributes
import nl.jolanrensen.kHomeAssistant.entities.invoke
import kotlin.test.Test

class Test {

    class TestAutomation(kHass: KHomeAssistant) : Automation(kHass) {

        val denon_avrx2200w by MediaPlayer
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

//        runEvery(2.5.seconds) {
//
            println(entities)
//        }
//        Group["living_room_lights"].useAs(Light) {
//            color = Colors.RED
//            white_value = 100
//        }
        }
    }


    @Test
    fun `Basic kHomeAssistant run`() = runBlocking {
        kHomeAssistant.run(
            TestAutomation(kHomeAssistant)
        )
        assert(true)
    }

    @Test
    fun `Other test`() {
        println(HassAttributes::class.members)
    }

}