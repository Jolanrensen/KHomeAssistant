package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMotionSensorState.MOTION
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMotionSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class MotionBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryMotionSensorState, MotionBinarySensor.Entity>() {

    /** Making sure MotionSensor acts as a singleton. */
    override fun equals(other: Any?) = other is MotionBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "motion".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryMotionSensorState>(
        getKHass = getKHass,
        name = name,
        domain = MotionBinarySensor(getKHass),
        deviceClass = "motion"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryMotionSensorState.parseState(it) }


        fun onMotionDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(MOTION, callback)

        fun onTurnedClear(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLEAR, callback)
    }
}

sealed class BinaryMotionSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryMotionSensorState = when (onOff) {
            OnOff.ON -> MOTION
            OnOff.OFF -> CLEAR
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Motion is detected */
    object MOTION : BinaryMotionSensorState(OnOff.ON)

    /** No motion is detected */
    object CLEAR : BinaryMotionSensorState(OnOff.OFF)
}

val HasKHassContext.MotionBinarySensor: MotionBinarySensor
    get() = MotionBinarySensor(getKHass)