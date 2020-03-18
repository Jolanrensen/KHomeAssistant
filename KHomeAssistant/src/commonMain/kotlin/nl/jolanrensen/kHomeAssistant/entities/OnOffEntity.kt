package nl.jolanrensen.kHomeAssistant.entities

interface OnOffEntity {
    fun turnOn()

    fun turnOff()

    fun toggle()


    /** HelperFunctions */
    val isOn: Boolean

    val ifOff: Boolean

    val isUnavailable: Boolean
}