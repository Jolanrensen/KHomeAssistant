package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.minutes
import com.soywiz.klock.plus
import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.domains.getValue
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.invoke
import kotlin.test.Test

class Test {

    class TestAutomation(kHass: KHomeAssistant) : Automation(kHass) {

        val denon_avrx2200w by MediaPlayer
        val bothDateAndTime = InputDatetime["both_date_and_time"]
        val onlyDate = InputDatetime["only_date"]
        val onlyTime = InputDatetime.Entity("only_time")

        override suspend fun initialize() {
            println(denon_avrx2200w)
            println(bothDateAndTime)
            println(onlyDate)
            println(onlyTime)

//            onEventFired("call_service") {
//                println("call service called!")
//                println(it)
//            }

            onlyTime {
                time += 1.minutes

                println(this)

            }

        }
    }

    val kHomeAssistant = KHomeAssistantInstance(
        host = "home.jolanrensen.nl",
        port = 8123,
        secure = true,
        debug = false,
        accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI"
    )

    @Test
    fun `Basic kHomeAssistant run`() = runBlocking {
        kHomeAssistant.run(
            TestAutomation(kHomeAssistant)
        )
        assert(true)
    }

}