package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGarageDoorSensorState.OPEN
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGarageDoorSensorState.CLOSED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class GarageDoorBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryGarageDoorSensorState, GarageDoorBinarySensor.Entity>() {

    /** Making sure GarageDoorSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GarageDoorBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "garage_door".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryGarageDoorSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = GarageDoorBinarySensor(getKHomeAssistant),
        deviceClass = "garage_door"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryGarageDoorSensorState.parseState(it) }


        fun onOpened(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OPEN, callback)

        fun onClosed(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLOSED, callback)
    }
}

sealed class BinaryGarageDoorSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryGarageDoorSensorState = when (onOff) {
            OnOff.ON -> OPEN
            OnOff.OFF -> CLOSED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Garage door is opened */
    object OPEN : BinaryGarageDoorSensorState(OnOff.ON)

    /** Garage door is closed */
    object CLOSED : BinaryGarageDoorSensorState(OnOff.OFF)
}

val HasContext.GarageDoorBinarySensor: GarageDoorBinarySensor
    get() = GarageDoorBinarySensor(getKHomeAssistant)