package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.serialization.KSerializer
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.LightAttributes
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.domains.LightDomain


class LightEntity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
) : Entity<OnOff, LightAttributes>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = LightDomain // TODO check?
), ToggleEntity, KHomeAssistantContext {

    override val attributesSerializer = LightAttributes.serializer()

    override fun getStateValue(state: OnOff) = state.stateValue

    override fun parseStateValue(stateValue: String) = try {
        OnOff.values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    override fun onTurnOn(callback: ToggleEntity.() -> Unit) {
        TODO("Not yet implemented")
    }

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

/**  Instantiate Light from a WithKHomeAssistant context without having to specify it. */
// TODO not sure whether to keep this or to just use Light.Entity
fun KHomeAssistantContext.LightEntity(name: String) = LightEntity(
        kHomeAssistant = kHomeAssistant,
        name = name
)