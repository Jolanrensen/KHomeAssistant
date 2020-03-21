package nl.jolanrensen.kHomeAssistant.entities

interface ToggleEntity {
    fun turnOn()

    fun turnOff()

    fun toggle()


    /** HelperFunctions */
    fun isOn(): Boolean

    fun ifOff(): Boolean

    fun isUnavailable(): Boolean
}