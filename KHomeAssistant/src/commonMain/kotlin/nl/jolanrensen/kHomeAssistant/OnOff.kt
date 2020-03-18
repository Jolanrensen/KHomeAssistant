package nl.jolanrensen.kHomeAssistant

import nl.jolanrensen.kHomeAssistant.states.State


// TODO check if this or sealed class is needed
enum class OnOff(override val state: String) : State<String> {
    ON("on"),
    OFF("off"),
    UNAVAILABLE("unavailable")
    // TODO check if other states are possible
}