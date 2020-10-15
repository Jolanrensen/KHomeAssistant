@file:OptIn(ExperimentalTime::class)

import com.soywiz.korio.async.async
import examples.BedroomLights
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.domains.getValue
import nl.jolanrensen.kHomeAssistant.domains.input.InputBoolean
import nl.jolanrensen.kHomeAssistant.runEvery
import java.util.concurrent.Executors
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.reflect
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


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

    val cast_radio by InputBoolean

    override suspend fun initialize() {
        println(denon_avrx2200w)
    }
}


fun main() = runBlocking {
    println("running!")

    kHomeAssistant.run(
        TestAutomation(kHomeAssistant)
//        BedroomLights(kHomeAssistant)
//        AutoLights(kHomeAssistant)
    ).join()
}

