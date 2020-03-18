package nl.jolanrensen.kHomeAssistant

open class Automation : WithKHomeAssistant {

    open val automationName: String
        get() = this::class.simpleName.toString()

    override lateinit var kHomeAssistant: KHomeAssistant

    /**
     * This method is called to start the automation
     * and it should thus contain the setup of all listeners.
     */
    open suspend fun initialize() = Unit

}

/** Functional invokation of Automation */
fun automation(automationName: String, initialize: suspend Automation.() ->  Unit) = object : Automation() {
    override val automationName = automationName
    override suspend fun initialize() = initialize(this)
}