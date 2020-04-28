
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
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


fun main() {
    runBlocking {
        println("running!")

        KHomeAssistant(
            host = "home.jolanrensen.nl",
            port = 8123,
            secure = true,
            debug = false,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                automation("1") {

                    val batik = Light["batik"] {
                        onAttributeChanged(::brightness) {

                        }

                        ::brightness.onChanged(this) {
                            println("brightness changed to $brightness")
                        }
                    }

                }
            )
        ).run()
    }

}