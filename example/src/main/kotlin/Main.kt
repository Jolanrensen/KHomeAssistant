import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.entities.Light


class Test : Automation() {


    override suspend fun initialize() {
        val a: String? = null

//        val dreamWorld = Switch("10006b21d4").getAttributes()["friendly_name"]!!.primitive.content

//        val switchState = dreamWorld.getState()!!
//
//        println(switchState.state)


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
            debug = true,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf(
                    Test(),
                    automation("some automation") {
                        println("TEST inline automation")

                        val internallyDefinedLight = Light("wall_lamp").getState()

                    }
            )
    )

    val externallyDefinedLight = Light(kHomeAssistant, "some_light")

    kHomeAssistant.automations.add(
            automation("Some automation thing") {
                //externallyDefinedLight.turnOff()
            }
    )

    kHomeAssistant.run()
}