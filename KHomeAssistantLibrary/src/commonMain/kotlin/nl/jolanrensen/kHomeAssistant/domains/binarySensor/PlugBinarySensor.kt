package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPlugSensorState.PLUGGED
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPlugSensorState.UNPLUGGED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class PlugBinarySensor(override val kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryPlugSensorState, PlugBinarySensor.Entity>(kHassInstance) {

    /** Making sure PlugSensor acts as a singleton. */
    override fun equals(other: Any?) = other is PlugBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "plug".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryPlugSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = PlugBinarySensor(kHassInstance),
        expectedDeviceClass = BinarySensorDeviceClass.PLUG
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryPlugSensorState.parseState(it) }


        fun onPluggedIn(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(PLUGGED, callback)

        fun onUnplugged(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(UNPLUGGED, callback)
    }
}

sealed class BinaryPlugSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryPlugSensorState = when (onOff) {
            OnOff.ON -> PLUGGED
            OnOff.OFF -> UNPLUGGED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Device is plugged in */
    object PLUGGED : BinaryPlugSensorState(OnOff.ON)

    /** Device is unplugged */
    object UNPLUGGED : BinaryPlugSensorState(OnOff.OFF)
}

val KHomeAssistant.PlugBinarySensor: PlugBinarySensor
    get() = PlugBinarySensor(this)