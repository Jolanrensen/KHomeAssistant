
import com.soywiz.klock.DateTime
import com.soywiz.klock.minutes
import kotlinx.coroutines.runBlocking
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.InputBoolean
import nl.jolanrensen.kHomeAssistant.domains.InputNumber
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn
import nl.jolanrensen.kHomeAssistant.runAt


class BedroomLights : Automation() {

    val allLights: List<Light.Entity> = Light["bed", "bedroom_lamp", "globe", "pisa"]

    override suspend fun initialize() {

        allLights {
            println(state)
        }

        Switch["bedroom_switch"].onTurnOn {
            if (allLights.any { it.isOn })
                allLights.turnOff()
            else
                allLights.turnOn()

            turnOff()
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

                    runAt((DateTime.now() + .5.minutes).local) {
                        println("Half a minute passed!")
                    }

                    InputNumber["input_number_test"] {
                        println(this)
                    }

                    Light["batik"] {
                        println(this)
                    }



                    var toiletWindow by InputBoolean["toilet_window"]


                }
            )
        ).run()
    }

}