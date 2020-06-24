package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryLightSensorState.LIGHT
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryLightSensorState.NO_LIGHT
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class LightBinarySensor(kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryLightSensorState, LightBinarySensor.Entity>(kHassInstance) {

    /** Making sure LightSensor acts as a singleton. */
    override fun equals(other: Any?) = other is LightBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "light".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryLightSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = LightBinarySensor(kHassInstance),
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

val KHomeAssistant.LightBinarySensor: LightBinarySensor
    get() = LightBinarySensor(this)