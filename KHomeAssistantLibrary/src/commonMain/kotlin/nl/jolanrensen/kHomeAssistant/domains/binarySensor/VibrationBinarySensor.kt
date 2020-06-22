package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryVibrationSensorState.VIBRATION
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryVibrationSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class VibrationBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryVibrationSensorState, VibrationBinarySensor.Entity>() {

    /** Making sure VibrationSensor acts as a singleton. */
    override fun equals(other: Any?) = other is VibrationBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "vibration".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryVibrationSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = VibrationBinarySensor(getKHomeAssistant),
        deviceClass = "vibration"
    ) {
        override fun parseStateValue(stateValue: String) =
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
    get() = VibrationBinarySensor(getKHomeAssistant)