package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.SwitchAttributes
import nl.jolanrensen.kHomeAssistant.domains.Switch
import nl.jolanrensen.kHomeAssistant.domains.SwitchDomain

class SwitchEntity(
        override val kHomeAssistant: KHomeAssistant,
        override val name: String
) : Entity<OnOff, SwitchAttributes>(
        kHomeAssistant = kHomeAssistant,
        domain = kHomeAssistant.Switch,
        name = name
), ToggleEntity, KHomeAssistantContext {

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

    override fun isOn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun ifOff(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isUnavailable(): Boolean {
        TODO("Not yet implemented")
    }

}

/**  Instantiate Switch from a WithKHomeAssistant context without having to specify it. */
fun KHomeAssistantContext.SwitchEntity(name: String) = SwitchEntity(
        kHomeAssistant = kHomeAssistant,
        name = name
)