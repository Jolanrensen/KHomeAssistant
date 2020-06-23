package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryOpeningSensorState.OPEN
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryOpeningSensorState.CLOSED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class OpeningBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryOpeningSensorState, OpeningBinarySensor.Entity>() {

    /** Making sure OpeningSensor acts as a singleton. */
    override fun equals(other: Any?) = other is OpeningBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "opening".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryOpeningSensorState>(
        getKHass = getKHass,
        name = name,
        domain = OpeningBinarySensor(getKHass),
        deviceClass = "opening"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryOpeningSensorState.parseState(it) }


        fun onOpened(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OPEN, callback)

        fun onClosed(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLOSED, callback)
    }
}

sealed class BinaryOpeningSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryOpeningSensorState = when (onOff) {
            OnOff.ON -> OPEN
            OnOff.OFF -> CLOSED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Opening is opened */
    object OPEN : BinaryOpeningSensorState(OnOff.ON)

    /** Opening is closed */
    object CLOSED : BinaryOpeningSensorState(OnOff.OFF)
}

val HasKHassContext.OpeningBinarySensor: OpeningBinarySensor
    get() = OpeningBinarySensor(getKHass)