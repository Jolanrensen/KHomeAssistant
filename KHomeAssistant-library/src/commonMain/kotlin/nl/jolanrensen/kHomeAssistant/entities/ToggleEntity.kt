package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.OnOff.*
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage


open class ToggleEntity(
    override val kHomeAssistant: () -> KHomeAssistant?,
    override val name: String,
    override val domain: Domain<BaseEntity<OnOff>>
) : BaseEntity<OnOff>(
    kHomeAssistant = kHomeAssistant,
    name = name,
    domain = domain
) {
    override fun getStateValue(state: OnOff): String = state.stateValue

    override fun parseStateValue(stateValue: String): OnOff? = try {
        values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    /** Turns on an entity (that supports being turned on), for example an automation, switch, etc */
    suspend inline fun turnOn(async: Boolean = false): ResultMessage {
        val result = callService("turn_on")
        if (!async) suspendUntilStateChangedTo(ON)
        return result
    }

    /** Turns off an entity (that supports being turned off), for example an automation, switch, etc */
    suspend inline fun turnOff(async: Boolean = false): ResultMessage {
        val result = callService("turn_off")
        if (!async) suspendUntilStateChangedTo(OFF)
        return result
    }

    /** Turns off an entity that is on, or turns on an entity that is off (that supports being turned on and off) */
    suspend inline fun toggle(async: Boolean = false): ResultMessage {
        val oldState = state
        val result = callService("toggle")
        if (!async) suspendUntilStateChangedTo(
            when (oldState) {
                ON -> OFF
                OFF -> ON
                UNAVAILABLE -> ON
            }
        )
        return result
    }

    /** state can also be writable. */
    override var state: OnOff
        get() = super.state
        set(value) {
            runBlocking { switchTo(value) }
        }

    suspend inline fun switchTo(state: OnOff) {
        when (state) {
            ON -> turnOn()
            OFF -> turnOff()
            else -> Unit
        }
    }

    suspend inline fun switchTo(on: Boolean) {
        if (on) turnOn() else turnOff()
    }

    /** HelperFunctions */
    var isOn: Boolean
        get() = state == ON
        set(value) {
            runBlocking { switchTo(value) }
        }

    var isOff: Boolean
        get() = state == OFF
        set(value) {
            runBlocking { switchTo(!value) }
        }

    val isUnavailable: Boolean
        get() = state == OnOff.UNAVAILABLE
}

fun <E : ToggleEntity> E.onTurnOn(callback: suspend E.() -> Unit) =
    onStateChangedTo(ON, callback)

fun <E : ToggleEntity> E.onTurnOff(callback: suspend E.() -> Unit) =
    onStateChangedTo(OFF, callback)

fun <E : ToggleEntity> E.onUnavailable(callback: suspend E.() -> Unit) =
    onStateChangedTo(OnOff.UNAVAILABLE, callback)

suspend inline fun <E : ToggleEntity> Iterable<E>.turnOn() = this { turnOn() }
suspend inline fun <E : ToggleEntity> Iterable<E>.turnOff() = this { turnOff() }
suspend inline fun <E : ToggleEntity> Iterable<E>.toggle() = this { toggle() }
suspend inline fun <E : ToggleEntity> Iterable<E>.switchTo(state: OnOff) = this { switchTo(state) }
suspend inline fun <E : ToggleEntity> Iterable<E>.switchTo(state: Boolean) = this { switchTo(state) }