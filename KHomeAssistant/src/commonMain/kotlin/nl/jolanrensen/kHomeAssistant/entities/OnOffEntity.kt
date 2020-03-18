package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.states.OnOffState

interface OnOffEntity  {
    fun turnOn()

    fun turnOff()

    fun toggle()


    /** HelperFunctions */
    val isOn: Boolean

    val ifOff: Boolean

    val isUnavailable: Boolean
}