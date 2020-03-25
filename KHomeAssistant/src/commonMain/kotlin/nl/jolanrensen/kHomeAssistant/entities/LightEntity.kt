package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.LightAttributes
import nl.jolanrensen.kHomeAssistant.domains.LightDomain


class LightEntity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
) : BaseEntity<OnOff, LightAttributes>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = LightDomain // TODO check?
), ToggleEntity {

    override val attributesSerializer = LightAttributes.serializer()

    override fun getStateValue(state: OnOff) = state.stateValue

    override fun parseStateValue(stateValue: String) = try {
        OnOff.values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    override fun onTurnOn(callback: suspend ToggleEntity.() -> Unit) {

    }

    override suspend fun turnOn() {
        callService("turn_on")
    }

    suspend fun turnOn(brightness: Int /* TODO ETC */) {
        callService(
                serviceName = "turn_on",
                data = mapOf(
                        "brightness" to JsonPrimitive(brightness)
                )
        )
    }

    override suspend fun turnOff() {
        callService("turn_off")
    }

    override suspend fun toggle() {
        if (isOn()) turnOff() else turnOn()
    }

    override suspend fun isOn() = getState() == OnOff.ON

    override suspend fun isOff() = getState() == OnOff.OFF

    override suspend fun isUnavailable() = getState() == OnOff.UNAVAILABLE


}

/**  Instantiate Light from a WithKHomeAssistant context without having to specify it. */
// TODO not sure whether to keep this or to just use Light.Entity
fun KHomeAssistantContext.LightEntity(name: String) = LightEntity(
        kHomeAssistant = kHomeAssistant,
        name = name
)