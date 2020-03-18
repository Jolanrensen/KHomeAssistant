package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.states.OnOffState
import nl.jolanrensen.kHomeAssistant.states.SwitchState

class Switch(
        override val kHomeAssistant: KHomeAssistant,
        override val name: String
) : Entity<SwitchState>(
        kHomeAssistant = kHomeAssistant,
        domain = "switch",
        name = name
), OnOffEntity, WithKHomeAssistant {

//    override var state: SwitchState
//        get() = kHomeAssistant.getState(this)!!
//        set(value) {}

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