package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain

open class ToggleEntity(
    override val kHomeAssistant: () -> KHomeAssistant?,
    override val name: String,
    override val domain: Domain<out BaseEntity<OnOff>>
) : BaseEntity<OnOff>(
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

    /** Turns on an entity (that supports being turned on), for example an automation, switch, etc */
    suspend inline fun turnOn() = callService("turn_on")

    /** Turns off an entity (that supports being turned off), for example an automation, switch, etc */
    suspend inline fun turnOff() = callService("turn_off")

    /** Turns off an entity that is on, or turns on an entity that is off (that supports being turned on and off) */
    suspend inline fun toggle() = callService("toggle")

    suspend inline fun turn(state: OnOff) {
        when (state) {
            OnOff.ON -> turnOn()
            OnOff.OFF -> turnOff()
        }
    }

    suspend inline fun turn(state: Boolean) {
        if (state) turnOn() else turnOff()
    }

    /** HelperFunctions */
    var isOn: Boolean
        get() = state == OnOff.ON
        set(value) { runBlocking { turn(value) } }

    var isOff: Boolean
        get() = state == OnOff.OFF
        set(value) { runBlocking { turn(!value) } }

    val isUnavailable: Boolean
        get() = state == OnOff.UNAVAILABLE
}

fun <E : ToggleEntity> E.onTurnOn(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.ON, callback)

fun <E : ToggleEntity> E.onTurnOff(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.OFF, callback)

fun <E : ToggleEntity> E.onUnavailable(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.UNAVAILABLE, callback)

suspend inline fun <E: ToggleEntity> Iterable<E>.turnOn() = this { turnOn() }
suspend inline fun <E: ToggleEntity> Iterable<E>.turnOff() = this { turnOff() }
suspend inline fun <E: ToggleEntity> Iterable<E>.toggle() = this { toggle() }
suspend inline fun <E: ToggleEntity> Iterable<E>.turn(state: OnOff) = this { turn(state) }
suspend inline fun <E: ToggleEntity> Iterable<E>.turn(state: Boolean) = this { turn(state) }