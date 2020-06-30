package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySafetySensorState.UNSAFE
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySafetySensorState.SAFE
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class SafetyBinarySensor(override val kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinarySafetySensorState, SafetyBinarySensor.Entity>(kHassInstance) {

    /** Making sure SafetySensor acts as a singleton. */
    override fun equals(other: Any?) = other is SafetyBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "safety".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinarySafetySensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = SafetyBinarySensor(kHassInstance),
        deviceClass = "safety"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinarySafetySensorState.parseState(it) }


        fun onTurnedUnsafe(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(UNSAFE, callback)

        fun onTurnedSafe(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(SAFE, callback)
    }
}

sealed class BinarySafetySensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinarySafetySensorState = when (onOff) {
            OnOff.ON -> UNSAFE
            OnOff.OFF -> SAFE
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Unsafe */
    object UNSAFE : BinarySafetySensorState(OnOff.ON)

    /** Safe */
    object SAFE : BinarySafetySensorState(OnOff.OFF)
}

val KHomeAssistant.SafetyBinarySensor: SafetyBinarySensor
    get() = SafetyBinarySensor(this)