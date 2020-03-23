package nl.jolanrensen.kHomeAssistant

open class Automation : KHomeAssistantContext {

    open val automationName: String
        get() = this::class.simpleName.toString()

    override var kHomeAssistant: () -> KHomeAssistant? = { null }

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