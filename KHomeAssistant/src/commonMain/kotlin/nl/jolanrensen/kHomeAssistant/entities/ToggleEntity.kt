package nl.jolanrensen.kHomeAssistant.entities

interface ToggleEntity {
    fun turnOn()

    fun turnOff()

    fun toggle()

    fun onTurnOn(callback: ToggleEntity.() -> Unit)


    /** HelperFunctions */
    fun isOn(): Boolean

    fun ifOff(): Boolean

    fun isUnavailable(): Boolean
}