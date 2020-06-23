package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPlugSensorState.PLUGGED
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPlugSensorState.UNPLUGGED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class PlugBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryPlugSensorState, PlugBinarySensor.Entity>() {

    /** Making sure PlugSensor acts as a singleton. */
    override fun equals(other: Any?) = other is PlugBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "plug".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryPlugSensorState>(
        getKHass = getKHass,
        name = name,
        domain = PlugBinarySensor(getKHass),
        deviceClass = "plug"
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

val HasKHassContext.PlugBinarySensor: PlugBinarySensor
    get() = PlugBinarySensor(getKHass)