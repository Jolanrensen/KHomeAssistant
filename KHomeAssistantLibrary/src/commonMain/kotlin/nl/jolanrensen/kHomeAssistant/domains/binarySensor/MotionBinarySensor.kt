package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMotionSensorState.MOTION
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMotionSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class MotionBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryMotionSensorState, MotionBinarySensor.Entity>() {

    /** Making sure MotionSensor acts as a singleton. */
    override fun equals(other: Any?) = other is MotionBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "motion".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryMotionSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = MotionBinarySensor(getKHomeAssistant),
        deviceClass = "motion"
    ) {
        override fun parseStateValue(stateValue: String) =
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
    get() = MotionBinarySensor(getKHomeAssistant)