package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryWindowSensorState.OPEN
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryWindowSensorState.CLOSED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class WindowBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryWindowSensorState, WindowBinarySensor.Entity>() {

    /** Making sure WindowSensor acts as a singleton. */
    override fun equals(other: Any?) = other is WindowBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "window".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryWindowSensorState>(
        getKHass = getKHass,
        name = name,
        domain = WindowBinarySensor(getKHass),
        deviceClass = "window"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryWindowSensorState.parseState(it) }


        fun onOpened(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OPEN, callback)

        fun onClosed(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLOSED, callback)
    }
}

sealed class BinaryWindowSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryWindowSensorState = when (onOff) {
            OnOff.ON -> OPEN
            OnOff.OFF -> CLOSED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Window is opened */
    object OPEN : BinaryWindowSensorState(OnOff.ON)

    /** Window is closed */
    object CLOSED : BinaryWindowSensorState(OnOff.OFF)
}

val HasKHassContext.WindowBinarySensor: WindowBinarySensor
    get() = WindowBinarySensor(getKHass)