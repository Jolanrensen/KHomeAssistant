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

    /**  */

    override fun getStateValue(state: OnOff): String = state.stateValue

    override fun parseStateValue(stateValue: String): OnOff? = try {
        OnOff.values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    /** Turns on an entity (that supports being turned on), for example an automation, switch, etc */
    suspend inline fun turnOn() = callService("turn_on")

    /** Turns off an entity (that supports being turned off), for example an automation, switch, etc */
    suspend inline fun turnOff() = callService("turn_off")

    /** Turns off an entity that is on, or turns on an entity that is off (that supports being turned on and off) */
    suspend inline fun toggle() = callService("toggle")


    /** HelperFunctions */
    val isOn get() = state == OnOff.ON

    val isOff get() = state == OnOff.OFF

    val isUnavailable get() = state == OnOff.UNAVAILABLE
}

fun <A : BaseAttributes, E : ToggleEntity<A>> E.onTurnOn(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.ON, callback)

fun <A : BaseAttributes, E : ToggleEntity<A>> E.onTurnOff(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.OFF, callback)

fun <A : BaseAttributes, E : ToggleEntity<A>> E.onUnavailable(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.UNAVAILABLE, callback)