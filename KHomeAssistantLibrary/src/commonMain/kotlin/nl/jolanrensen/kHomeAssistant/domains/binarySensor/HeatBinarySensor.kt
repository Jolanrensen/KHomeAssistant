package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryHeatSensorState.HOT
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryHeatSensorState.NORMAL
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class HeatBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryHeatSensorState, HeatBinarySensor.Entity>() {

    /** Making sure HeatSensor acts as a singleton. */
    override fun equals(other: Any?) = other is HeatBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "heat".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryHeatSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = HeatBinarySensor(getKHomeAssistant),
        deviceClass = "heat"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryHeatSensorState.parseState(it) }


        fun onTurnedHot(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(HOT, callback)

        fun onTurnedNormal(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(NORMAL, callback)
    }
}

sealed class BinaryHeatSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryHeatSensorState = when (onOff) {
            OnOff.ON -> HOT
            OnOff.OFF -> NORMAL
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Hot */
    object HOT : BinaryHeatSensorState(OnOff.ON)

    /** Normal */
    object NORMAL : BinaryHeatSensorState(OnOff.OFF)
}

val HasKHassContext.HeatBinarySensor: HeatBinarySensor
    get() = HeatBinarySensor(getKHomeAssistant)