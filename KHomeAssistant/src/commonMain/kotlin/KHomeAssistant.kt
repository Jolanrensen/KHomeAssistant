/**
 * KHomeAssistant instance
 *
 * run run() to make the instance run
 */
class KHomeAssistant(
        val host: String,
        val port: Int = 8123,
        val accessToken: String,
        val automations: List<Automation>
) {

//    val stateListeners

    fun run() {
        connect()
        initializeAutomations()

    }

    private fun connect() {

    }

    private fun initializeAutomations() {
        for (it in automations) {
            try {
                it.kHomeAssistant = this
                it.initialize()
                println("Successfully initialized automation ${it::class.simpleName}")
            } catch (e: Exception) {
                println("FAILED to initialize automation ${it::class.simpleName}")
            }
        }
    }


}