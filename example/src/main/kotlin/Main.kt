import kotlinx.coroutines.delay
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.Switch


class Test : Automation() {

    val bedroomLamp = Light.Entity("bedroom_lamp")
    val bed = Light.Entity("bed")
    val globe = Light.Entity("globe")

    val allLights = listOf(bedroomLamp, bed, globe)

    override suspend fun initialize() {
        Switch.Entity("bedroom_switch").onTurnOn {
            if (allLights.any { it.isOn() })
                allLights.forEach { it.turnOff() }
            else
                allLights.forEach { it.turnOn() }

            turnOff()
        }
    }

}


suspend fun main() {

//    val test = Entity()
    println("running!")

//    KHomeAssistant(
//            host = "home.jolanrensen.nl",
//            port = 8123,
//            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
//            secure = true,
//            debug = true,
//            automationName = "Turn on light"
//    ) {
//        Light("wall_lamp").turnOn()
//    }

    val kHomeAssistant = KHomeAssistant(
            host = "home.jolanrensen.nl",
            port = 8123,
            secure = true,
            debug = false,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
//                    automation("example test") {
//                        val example = Example.Entity("test")
//                        val state = example.getState()
//                    },
                    automation("some automation") {
                        val batik = Light.Entity("batik")
                        println("wall lamp is currently ${batik.getState()}")

                        val attrs = batik.getAttributes()[""]!!
                        println("attributes are: $attrs")

                        val test = Domain("light").Entity("wall_lamp").getAttributes()

                        println("Wall lamp default test, state: ${test.fullJsonObject}")

                    }
            )
    )

//    val externallyDefinedLight = Light(kHomeAssistant).Entity("some_light")

//    kHomeAssistant.automations.add(
//            automation("Some automation thing") {
//                //externallyDefinedLight.turnOff()
//            }
//    )

    kHomeAssistant.run()
}