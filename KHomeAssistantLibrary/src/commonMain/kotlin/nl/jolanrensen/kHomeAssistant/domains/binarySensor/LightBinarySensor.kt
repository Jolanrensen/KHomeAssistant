package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryLightSensorState.LIGHT
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryLightSensorState.NO_LIGHT
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class LightBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryLightSensorState, LightBinarySensor.Entity>() {

    /** Making sure LightSensor acts as a singleton. */
    override fun equals(other: Any?) = other is LightBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "light".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryLightSensorState>(
        getKHass = getKHass,
        name = name,
        domain = LightBinarySensor(getKHass),
        deviceClass = "light"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryLightSensorState.parseState(it) }


        fun onLightDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(LIGHT, callback)

        fun onNoLightDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(NO_LIGHT, callback)
    }
}

sealed class BinaryLightSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryLightSensorState = when (onOff) {
            OnOff.ON -> LIGHT
            OnOff.OFF -> NO_LIGHT
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Light is detected */
    object LIGHT : BinaryLightSensorState(OnOff.ON)

    /** No light is detected */
    object NO_LIGHT : BinaryLightSensorState(OnOff.OFF)
}

val HasKHassContext.LightBinarySensor: LightBinarySensor
    get() = LightBinarySensor(getKHass)