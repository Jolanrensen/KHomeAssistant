package nl.jolanrensen.kHomeAssistant.entities

interface ToggleEntity {
    suspend fun turnOn()

    suspend fun turnOff()

    suspend fun toggle()

    fun onTurnOn(callback: suspend ToggleEntity.() -> Unit)


    /** HelperFunctions */
    suspend fun isOn(): Boolean

    suspend fun isOff(): Boolean

    suspend fun isUnavailable(): Boolean
}