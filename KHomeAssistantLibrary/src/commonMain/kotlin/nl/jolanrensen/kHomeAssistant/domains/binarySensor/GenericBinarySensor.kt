package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGenericSensorState.ON
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGenericSensorState.OFF
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class GenericBinarySensor(kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryGenericSensorState, GenericBinarySensor.Entity>(kHassInstance) {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryGenericSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = GenericBinarySensor(kHassInstance),
        deviceClass = null
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryGenericSensorState.parseState(it) }


        fun onTurnedOn(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(ON, callback)

        fun onTurnedOff(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OFF, callback)
    }
}

sealed class BinaryGenericSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryGenericSensorState = when (onOff) {
            OnOff.ON -> ON
            OnOff.OFF -> OFF
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** On */
    object ON : BinaryGenericSensorState(OnOff.ON)

    /** Off */
    object OFF : BinaryGenericSensorState(OnOff.OFF)
}

val KHomeAssistant.GenericBinarySensor: GenericBinarySensor
    get() = GenericBinarySensor(this)