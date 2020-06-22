package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryBatteryChargingSensorState.CHARGING
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryBatteryChargingSensorState.NOT_CHARGING
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class BatteryChargingBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryBatteryChargingSensorState, BatteryChargingBinarySensor.Entity>() {

    /** Making sure BatteryChargingSensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatteryChargingBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery_charging".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryBatteryChargingSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = BatteryChargingBinarySensor(getKHomeAssistant),
        deviceClass = "battery_charging"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryBatteryChargingSensorState.parseState(it) }


        fun onStartedCharging(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CHARGING, callback)

        fun onStoppedCharging(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(NOT_CHARGING, callback)
    }
}

sealed class BinaryBatteryChargingSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryBatteryChargingSensorState = when (onOff) {
            OnOff.ON -> CHARGING
            OnOff.OFF -> NOT_CHARGING
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Battery is charging */
    object CHARGING : BinaryBatteryChargingSensorState(OnOff.ON)

    /** Battery is not charging */
    object NOT_CHARGING : BinaryBatteryChargingSensorState(OnOff.OFF)
}

val HasKHassContext.BatteryChargingBinarySensor: BatteryChargingBinarySensor
    get() = BatteryChargingBinarySensor(getKHomeAssistant)