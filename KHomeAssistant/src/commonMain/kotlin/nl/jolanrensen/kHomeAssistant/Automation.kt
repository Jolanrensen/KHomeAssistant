package nl.jolanrensen.kHomeAssistant

import kotlin.coroutines.CoroutineContext

open class Automation : KHomeAssistantContext {

    open val automationName: String
        get() = this::class.simpleName.toString()

    override var kHomeAssistant: () -> KHomeAssistant? = { null }

    override val coroutineContext: CoroutineContext
        get() = kHomeAssistant()!!.coroutineContext

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