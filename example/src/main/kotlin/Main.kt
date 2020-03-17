import nl.jolanrensen.kHomeAssistant.entities.Entity


class Test : Automation() {
    override fun initialize() {
        val a: String? = null



    }

}

fun main() {

    val test = Entity()
    println("running!")

    KHomeAssistant(
            host = "home.jolanrensen.nl",
            port = 8123,
            accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI0ZTQzYjAwYzc2Njc0ODgzOTBlZTRkNWFmMzgxZGJhNiIsImlhdCI6MTU4NDQ0OTE4NywiZXhwIjoxODk5ODA5MTg3fQ.NaDfDicsHwdpsppIBGQ06moDulGV3K6jFn3ViQDcRwI",
            automations = listOf<Automation>(Test())
    ).run()
}