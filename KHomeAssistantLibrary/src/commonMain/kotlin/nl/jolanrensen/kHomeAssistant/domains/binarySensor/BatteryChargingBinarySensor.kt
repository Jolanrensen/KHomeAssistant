package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryBatteryChargingSensorState.CHARGING
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryBatteryChargingSensorState.NOT_CHARGING
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class BatteryChargingBinarySensor(override val kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryBatteryChargingSensorState, BatteryChargingBinarySensor.Entity>(kHassInstance) {

    /** Making sure BatteryChargingSensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatteryChargingBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery_charging".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryBatteryChargingSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = BatteryChargingBinarySensor(kHassInstance),
        expectedDeviceClass = BinarySensorDeviceClass.BATTERY_CHARGING
    ) {
        override fun stringToState(stateValue: String) =
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

val KHomeAssistant.BatteryChargingBinarySensor: BatteryChargingBinarySensor
    get() = BatteryChargingBinarySensor(this)