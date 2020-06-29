package nl.jolanrensen.kHomeAssistant.entities

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.OnOff.*
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage


open class ToggleEntity<AttrsType : HassAttributes>(
    kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: Domain<Entity<OnOff, AttrsType>>
) : BaseEntity<OnOff, AttrsType>(
    kHassInstance = kHassInstance,
    name = name,
    domain = domain
) {
    override fun stateToString(state: OnOff): String = state.stateValue

    override fun stringToState(stateValue: String): OnOff? = try {
        values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    /** Turns on an entity (that supports being turned on), for example an automation, switch, etc */
    open suspend fun turnOn(async: Boolean = false): ResultMessage {
        val result = callService("turn_on")
        if (!async) suspendUntilStateChangedTo(ON)
        return result
    }

    /** Turns off an entity (that supports being turned off), for example an automation, switch, etc */
    open suspend fun turnOff(async: Boolean = false): ResultMessage {
        val result = callService("turn_off")
        if (!async) suspendUntilStateChangedTo(OFF)
        return result
    }

    /** Turns off an entity that is on, or turns on an entity that is off (that supports being turned on and off) */
    open suspend fun toggle(async: Boolean = false): ResultMessage {
        val oldState = state
        val result = callService("toggle")
        if (!async) suspendUntilStateChangedTo(
            when (oldState) {
                ON -> OFF
                OFF -> ON
                UNAVAILABLE, UNKNOWN -> ON
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

    suspend inline fun switchTo(state: OnOff, async: Boolean = false) {
        when (state) {
            ON -> turnOn(async)
            OFF -> turnOff(async)
            else -> Unit
        }
    }

    suspend inline fun switchTo(on: Boolean, async: Boolean = false) {
        if (on) turnOn(async) else turnOff(async)
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
        get() = state == UNAVAILABLE
}

fun <H : HassAttributes, E : ToggleEntity<H>> E.onTurnOn(callback: suspend E.() -> Unit) =
    onStateChangedTo(ON, callback)

fun <H : HassAttributes, E : ToggleEntity<H>> E.onTurnOff(callback: suspend E.() -> Unit) =
    onStateChangedTo(OFF, callback)

fun <H : HassAttributes, E : ToggleEntity<H>> E.onUnavailable(callback: suspend E.() -> Unit) =
    onStateChangedTo(UNAVAILABLE, callback)

suspend inline fun <H : HassAttributes, E : ToggleEntity<H>> Iterable<E>.turnOn(async: Boolean = false) =
    this { turnOn(async) }

suspend inline fun <H : HassAttributes, E : ToggleEntity<H>> Iterable<E>.turnOff(async: Boolean = false) =
    this { turnOff(async) }

suspend inline fun <H : HassAttributes, E : ToggleEntity<H>> Iterable<E>.toggle(async: Boolean = false) =
    this { toggle(async) }

suspend inline fun <H : HassAttributes, E : ToggleEntity<H>> Iterable<E>.switchTo(
    state: OnOff,
    async: Boolean = false
) = this { switchTo(state, async) }

suspend inline fun <H : HassAttributes, E : ToggleEntity<H>> Iterable<E>.switchTo(
    state: Boolean,
    async: Boolean = false
) = this { switchTo(state, async) }