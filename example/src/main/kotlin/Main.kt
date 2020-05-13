
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.domains.input.InputDatetime
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onTurnOn
import nl.jolanrensen.kHomeAssistant.entities.turnOff
import nl.jolanrensen.kHomeAssistant.entities.turnOn


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
            debug = true,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                automation("1") {

                    MediaPlayer["denon_avrx2200w"] {
//                        while (volume_level!! > 0)
//                            volume_level = volume_level!! - .005f;
                        isOff = true
                    }

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


//                    bothDateAndTime {
//                       while (true) {
//                           year = year!! + 1
//                       }
//                    }

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