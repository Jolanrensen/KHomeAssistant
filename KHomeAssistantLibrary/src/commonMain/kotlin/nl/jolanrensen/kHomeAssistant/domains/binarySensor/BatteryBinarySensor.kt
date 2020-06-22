package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryBatterySensorState.LOW
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryBatterySensorState.NORMAL
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class BatteryBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryBatterySensorState, BatteryBinarySensor.Entity>() {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatteryBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryBatterySensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = BatteryBinarySensor(getKHomeAssistant),
        deviceClass = "battery"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryBatterySensorState.parseState(it) }


        fun onLowLevel(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(LOW, callback)

        fun onNormalLevel(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(NORMAL, callback)
    }
}

sealed class BinaryBatterySensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryBatterySensorState = when (onOff) {
            OnOff.ON -> LOW
            OnOff.OFF -> NORMAL
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Low battery level */
    object LOW : BinaryBatterySensorState(OnOff.ON)

    /** High battery level */
    object NORMAL : BinaryBatterySensorState(OnOff.OFF)
}

val HasKHassContext.BatteryBinarySensor: BatteryBinarySensor
    get() = BatteryBinarySensor(getKHomeAssistant)