package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryOccupancySensorState.OCCUPIED
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryOccupancySensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class OccupancyBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryOccupancySensorState, OccupancyBinarySensor.Entity>() {

    /** Making sure OccupancySensor acts as a singleton. */
    override fun equals(other: Any?) = other is OccupancyBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "occupancy".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryOccupancySensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = OccupancyBinarySensor(getKHomeAssistant),
        deviceClass = "occupancy"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryOccupancySensorState.parseState(it) }


        fun onOccupied(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OCCUPIED, callback)

        fun onTurnedClear(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLEAR, callback)
    }
}

sealed class BinaryOccupancySensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryOccupancySensorState = when (onOff) {
            OnOff.ON -> OCCUPIED
            OnOff.OFF -> CLEAR
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Occupied */
    object OCCUPIED : BinaryOccupancySensorState(OnOff.ON)

    /** Unoccupied */
    object CLEAR : BinaryOccupancySensorState(OnOff.OFF)
}

val HasKHassContext.OccupancyBinarySensor: OccupancyBinarySensor
    get() = OccupancyBinarySensor(getKHomeAssistant)