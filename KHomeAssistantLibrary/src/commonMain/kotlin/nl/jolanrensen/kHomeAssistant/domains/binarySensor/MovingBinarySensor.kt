package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMovingSensorState.MOVING
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMovingSensorState.STOPPED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class MovingBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryMovingSensorState, MovingBinarySensor.Entity>() {

    /** Making sure MovingSensor acts as a singleton. */
    override fun equals(other: Any?) = other is MovingBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "moving".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryMovingSensorState>(
        getKHass = getKHass,
        name = name,
        domain = MovingBinarySensor(getKHass),
        deviceClass = "moving"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryMovingSensorState.parseState(it) }


        fun onStartedMoving(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(MOVING, callback)

        fun onStoppedMoving(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(STOPPED, callback)
    }
}

sealed class BinaryMovingSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryMovingSensorState = when (onOff) {
            OnOff.ON -> MOVING
            OnOff.OFF -> STOPPED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Moving */
    object MOVING : BinaryMovingSensorState(OnOff.ON)

    /** Not moving */
    object STOPPED : BinaryMovingSensorState(OnOff.OFF)
}

val HasKHassContext.MovingBinarySensor: MovingBinarySensor
    get() = MovingBinarySensor(getKHass)