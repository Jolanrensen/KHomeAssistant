package nl.jolanrensen.kHomeAssistant


enum class OnOff(val stateValue: String) {
    ON("on"),
    OFF("off"),
    UNAVAILABLE("unavailable")
    // TODO check if other states are possible
}