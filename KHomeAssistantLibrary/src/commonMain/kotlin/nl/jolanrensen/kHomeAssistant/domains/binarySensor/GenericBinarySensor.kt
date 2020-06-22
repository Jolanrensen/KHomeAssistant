package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGenericSensorState.ON
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGenericSensorState.OFF
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class GenericBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryGenericSensorState, GenericBinarySensor.Entity>() {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryGenericSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = GenericBinarySensor(getKHomeAssistant),
        deviceClass = null
    ) {
        override fun parseStateValue(stateValue: String) =
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

val HasKHassContext.GenericBinarySensor: GenericBinarySensor
    get() = GenericBinarySensor(getKHomeAssistant)