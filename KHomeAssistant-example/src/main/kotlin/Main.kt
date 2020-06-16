import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.domains.getValue
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn


class BedroomLights : Automation() {



//    val bed = Light.Entity("bed")
//    val bedroomLamp = Light.Entity("bedroom_lamp")
//    val globe = Light.Entity("globe")
//    val pisa = Light.Entity("pisa")
    
    override suspend fun initialize() {
        val (bed, bedroomLamp, globe, pisa) = Light["bed", "bedroom_lamp", "globe", "pisa"]
        val allLights = listOf(bed, bedroomLamp, globe, pisa)

        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn })
                allLights.turnOff()
            else
                allLights.turnOn()

            this.turnOff()
        }
    }
}

class TestAutomation : Automation() {

    val denon_avrx2200w by MediaPlayer
    val bothDateAndTime = InputDatetime["both_date_and_time"]
    val onlyDate = InputDatetime["only_date"]
    val onlyTime = InputDatetime.Entity("only_time")

    override suspend fun initialize() {
        println(denon_avrx2200w)
        println(bothDateAndTime)
        println(onlyDate)
        println(onlyTime)

    }
}

val kHomeAssistant = KHomeAssistant(
    host = "home.jolanrensen.nl",
    port = 8123,
    secure = true,
    debug = true,
    accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
    automations = listOf(TestAutomation())
)

fun main() {
    runBlocking {
        println("running!")
        kHomeAssistant.run()
    }

}