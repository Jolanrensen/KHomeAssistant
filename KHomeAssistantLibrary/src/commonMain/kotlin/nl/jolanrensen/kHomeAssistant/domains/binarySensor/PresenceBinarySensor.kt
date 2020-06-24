package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPresenceSensorState.HOME
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPresenceSensorState.AWAY
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class PresenceBinarySensor(kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryPresenceSensorState, PresenceBinarySensor.Entity>(kHassInstance) {

    /** Making sure PresenceSensor acts as a singleton. */
    override fun equals(other: Any?) = other is PresenceBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "presence".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryPresenceSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = PresenceBinarySensor(kHassInstance),
        deviceClass = "presence"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryPresenceSensorState.parseState(it) }


        fun onArrivedHome(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(HOME, callback)

        fun onLeftHome(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(AWAY, callback)
    }
}

sealed class BinaryPresenceSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryPresenceSensorState = when (onOff) {
            OnOff.ON -> HOME
            OnOff.OFF -> AWAY
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Home */
    object HOME : BinaryPresenceSensorState(OnOff.ON)

    /** Not home */
    object AWAY : BinaryPresenceSensorState(OnOff.OFF)
}

val KHomeAssistant.PresenceBinarySensor: PresenceBinarySensor
    get() = PresenceBinarySensor(this)