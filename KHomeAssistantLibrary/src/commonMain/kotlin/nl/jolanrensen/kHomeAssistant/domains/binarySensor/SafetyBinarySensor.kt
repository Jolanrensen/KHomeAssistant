package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySafetySensorState.UNSAFE
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySafetySensorState.SAFE
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class SafetyBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinarySafetySensorState, SafetyBinarySensor.Entity>() {

    /** Making sure SafetySensor acts as a singleton. */
    override fun equals(other: Any?) = other is SafetyBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "safety".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinarySafetySensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = SafetyBinarySensor(getKHomeAssistant),
        deviceClass = "safety"
    ) {
        override fun parseStateValue(stateValue: String) =
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

val HasKHassContext.SafetyBinarySensor: SafetyBinarySensor
    get() = SafetyBinarySensor(getKHomeAssistant)