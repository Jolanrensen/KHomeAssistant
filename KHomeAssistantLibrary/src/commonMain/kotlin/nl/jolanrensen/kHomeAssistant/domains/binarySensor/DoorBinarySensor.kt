package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryDoorSensorState.OPEN
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryDoorSensorState.CLOSED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class DoorBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryDoorSensorState, DoorBinarySensor.Entity>() {

    /** Making sure DoorSensor acts as a singleton. */
    override fun equals(other: Any?) = other is DoorBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "door".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryDoorSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = DoorBinarySensor(getKHomeAssistant),
        deviceClass = "door"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryDoorSensorState.parseState(it) }


        fun onOpened(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OPEN, callback)

        fun onClosed(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLOSED, callback)
    }
}

sealed class BinaryDoorSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryDoorSensorState = when (onOff) {
            OnOff.ON -> OPEN
            OnOff.OFF -> CLOSED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Door is opened */
    object OPEN : BinaryDoorSensorState(OnOff.ON)

    /** Door is closed */
    object CLOSED : BinaryDoorSensorState(OnOff.OFF)
}

val HasKHassContext.DoorBinarySensor: DoorBinarySensor
    get() = DoorBinarySensor(getKHomeAssistant)