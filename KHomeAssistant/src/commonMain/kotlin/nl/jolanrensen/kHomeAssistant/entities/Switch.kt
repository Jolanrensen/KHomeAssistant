package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.attributes.SwitchAttributes

class Switch(
        override val kHomeAssistant: KHomeAssistant,
        override val name: String
) : Entity<OnOff, SwitchAttributes>(
        kHomeAssistant = kHomeAssistant,
        domain = "switch",
        name = name
), OnOffEntity, WithKHomeAssistant {

    override fun getStateValue(state: OnOff) = state.stateValue

    override fun parseStateValue(stateValue: String) = try {
        OnOff.values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }


    override fun turnOn() {
        TODO("Not yet implemented")
    }

    override fun turnOff() {
        TODO("Not yet implemented")
    }

    override fun toggle() {
        TODO("Not yet implemented")
    }

    override val isOn: Boolean
        get() = TODO("Not yet implemented")
    override val ifOff: Boolean
        get() = TODO("Not yet implemented")
    override val isUnavailable: Boolean
        get() = TODO("Not yet implemented")
}

/**  Instantiate Switch from a WithKHomeAssistant context without having to specify it. */
fun WithKHomeAssistant.Switch(name: String) = Switch(
        kHomeAssistant = kHomeAssistant,
        name = name
)