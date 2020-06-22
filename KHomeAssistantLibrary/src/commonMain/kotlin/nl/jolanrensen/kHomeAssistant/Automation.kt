package nl.jolanrensen.kHomeAssistant

import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import kotlin.coroutines.CoroutineContext

abstract class Automation : HasKHassContext {

    open val automationName: String
        get() = this::class.simpleName.toString()

    var kHomeAssistantInstance: KHomeAssistant? = null

    override var getKHomeAssistant: () -> KHomeAssistant? = { kHomeAssistantInstance }

    override val coroutineContext: CoroutineContext
        get() = getKHomeAssistant()!!.coroutineContext

    /**
     * This method is called to start the automation
     * and it should thus contain the setup of all listeners.
     */
    abstract suspend fun initialize()

}

/** Functional invocation of Automation */
fun automation(automationName: String, initialize: suspend Automation.() ->  Unit) = object : Automation() {
    override val automationName = automationName
    override suspend fun initialize() = initialize(this)
}