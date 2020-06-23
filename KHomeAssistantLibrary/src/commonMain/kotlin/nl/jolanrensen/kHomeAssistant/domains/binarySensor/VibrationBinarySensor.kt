package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryVibrationSensorState.VIBRATION
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryVibrationSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class VibrationBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryVibrationSensorState, VibrationBinarySensor.Entity>() {

    /** Making sure VibrationSensor acts as a singleton. */
    override fun equals(other: Any?) = other is VibrationBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "vibration".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryVibrationSensorState>(
        getKHass = getKHass,
        name = name,
        domain = VibrationBinarySensor(getKHass),
        deviceClass = "vibration"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryVibrationSensorState.parseState(it) }


        fun onVibrationDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(VIBRATION, callback)

        fun onTurnedClear(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLEAR, callback)
    }
}

sealed class BinaryVibrationSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryVibrationSensorState = when (onOff) {
            OnOff.ON -> VIBRATION
            OnOff.OFF -> CLEAR
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Vibration is detected */
    object VIBRATION : BinaryVibrationSensorState(OnOff.ON)

    /** No vibration is detected */
    object CLEAR : BinaryVibrationSensorState(OnOff.OFF)
}

val HasKHassContext.VibrationBinarySensor: VibrationBinarySensor
    get() = VibrationBinarySensor(getKHass)