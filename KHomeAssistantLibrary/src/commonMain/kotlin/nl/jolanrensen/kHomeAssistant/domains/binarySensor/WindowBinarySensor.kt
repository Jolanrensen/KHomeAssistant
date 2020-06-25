package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryWindowSensorState.OPEN
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryWindowSensorState.CLOSED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class WindowBinarySensor(override val kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryWindowSensorState, WindowBinarySensor.Entity>(kHassInstance) {

    /** Making sure WindowSensor acts as a singleton. */
    override fun equals(other: Any?) = other is WindowBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "window".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryWindowSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = WindowBinarySensor(kHassInstance),
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

val KHomeAssistant.WindowBinarySensor: WindowBinarySensor
    get() = WindowBinarySensor(this)