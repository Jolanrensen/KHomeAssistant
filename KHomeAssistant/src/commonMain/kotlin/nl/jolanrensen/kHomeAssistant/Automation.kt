package nl.jolanrensen.kHomeAssistant

import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

open class Automation : KHomeAssistantContext {

    open val automationName: String
        get() = this::class.simpleName.toString()

    var kHomeAssistantInstance: KHomeAssistant? = null

    override var kHomeAssistant: () -> KHomeAssistant? = { kHomeAssistantInstance }

    override val coroutineContext: CoroutineContext
        get() = kHomeAssistant()!!.coroutineContext

    /**
     * This method is called to start the automation
     * and it should thus contain the setup of all listeners.
     */
    @OptIn(ExperimentalTime::class)
    open suspend fun initialize() = Unit

}

/** Functional invokation of Automation */
fun automation(automationName: String, initialize: suspend Automation.() ->  Unit) = object : Automation() {
    override val automationName = automationName
    override suspend fun initialize() = initialize(this)
}