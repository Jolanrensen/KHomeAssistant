package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.domains.Domain

open class ToggleEntity<AttributesType : BaseAttributes>(
    override val kHomeAssistant: () -> KHomeAssistant?,
    override val name: String,
    override val domain: Domain<out BaseEntity<OnOff, out AttributesType>>
) : BaseEntity<OnOff, AttributesType>(
    kHomeAssistant = kHomeAssistant,
    name = name,
    domain = domain
) {

    override fun getStateValue(state: OnOff): String = state.stateValue

    override fun parseStateValue(stateValue: String): OnOff? = try {
        OnOff.values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    suspend inline fun turnOn() = callService("turn_on")

    suspend inline fun turnOff() = callService("turn_off")

    suspend inline fun toggle() = callService("toggle")


    /** HelperFunctions */
    suspend inline fun isOn() = getState() == OnOff.ON

    suspend inline fun isOff() = getState() == OnOff.OFF

    suspend inline fun isUnavailable() = getState() == OnOff.UNAVAILABLE
}

fun <A : BaseAttributes, E : ToggleEntity<A>> E.onTurnOn(callback: suspend E.() -> Unit) =
    onStateChange({ it == OnOff.ON }, callback)

fun <A : BaseAttributes, E : ToggleEntity<A>> E.onTurnOff(callback: suspend E.() -> Unit) =
    onStateChange({ it == OnOff.OFF }, callback)

fun <A : BaseAttributes, E : ToggleEntity<A>> E.onUnavailable(callback: suspend E.() -> Unit) =
    onStateChange({ it == OnOff.UNAVAILABLE }, callback)