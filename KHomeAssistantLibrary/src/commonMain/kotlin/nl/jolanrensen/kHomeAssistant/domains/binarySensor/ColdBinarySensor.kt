package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryColdSensorState.COLD
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryColdSensorState.NORMAL
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class ColdBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryColdSensorState, ColdBinarySensor.Entity>() {

    /** Making sure ColdSensor acts as a singleton. */
    override fun equals(other: Any?) = other is ColdBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "cold".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryColdSensorState>(
        getKHass = getKHass,
        name = name,
        domain = ColdBinarySensor(getKHass),
        deviceClass = "cold"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryColdSensorState.parseState(it) }


        fun onTurnedCold(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(COLD, callback)

        fun onTurnedNormal(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(NORMAL, callback)
    }
}

sealed class BinaryColdSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryColdSensorState = when (onOff) {
            OnOff.ON -> COLD
            OnOff.OFF -> NORMAL
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Cold */
    object COLD : BinaryColdSensorState(OnOff.ON)

    /** Normal */
    object NORMAL : BinaryColdSensorState(OnOff.OFF)
}

val HasKHassContext.ColdBinarySensor: ColdBinarySensor
    get() = ColdBinarySensor(getKHass)