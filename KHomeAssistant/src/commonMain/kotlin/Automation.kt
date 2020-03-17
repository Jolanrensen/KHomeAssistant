

open class Automation {


    lateinit var kHomeAssistant: KHomeAssistant

    /**
     * This method is called to start the automation
     * and it should thus contain the setup of all listeners.
     */
    open fun initialize() = Unit

}