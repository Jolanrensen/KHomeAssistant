package nl.jolanrensen.kHomeAssistant

abstract class Automation(kHass: KHomeAssistant) : KHomeAssistant by kHass {

    open val automationName: String
        get() = this::class.simpleName.toString()

    /**
     * This method is called to start the automation
     * and it should thus contain the setup of all listeners.
     */
    abstract suspend fun initialize()
}

class FunctionalAutomation(val invoke: (kHass: KHomeAssistant) -> Automation)

/** Functional invocation of Automation where [KHomeAssistant] can be supplied later. */
fun automation(
    automationName: String,
    initialize: suspend Automation.() -> Unit
) = FunctionalAutomation { automation(it, automationName, initialize) }

/** Functional invocation of Automation */
fun automation(kHass: KHomeAssistant, automationName: String, initialize: suspend Automation.() -> Unit) =
    object : Automation(kHass) {
        override val automationName = automationName
        override suspend fun initialize() = initialize(this)
    }