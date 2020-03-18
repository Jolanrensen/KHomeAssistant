package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.WithKHomeAssistant
import nl.jolanrensen.kHomeAssistant.states.LightState
import nl.jolanrensen.kHomeAssistant.states.OnOffState


class Light(
        override val kHomeAssistant: KHomeAssistant,
        override val name: String
) : Entity<LightState>(
        kHomeAssistant = kHomeAssistant,
        domain = "light",
        name = name
), OnOffEntity, WithKHomeAssistant {

    class State()

//    override var state: LightState
//        get() = TODO()
//        set(value) {
//
//        }

    override fun turnOn() {
        TODO("Not yet implemented")
    }

    fun turnOn(brightness: Int? = null /* TODO ETC */) {

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

/**  Instantiate Light from a WithKHomeAssistant context without having to specify it. */
fun WithKHomeAssistant.Light(name: String) = Light(
        kHomeAssistant = kHomeAssistant,
        name = name
)