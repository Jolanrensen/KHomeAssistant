package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPowerSensorState.POWER
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryPowerSensorState.NO_POWER
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class PowerBinarySensor(kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryPowerSensorState, PowerBinarySensor.Entity>(kHassInstance) {

    /** Making sure PowerSensor acts as a singleton. */
    override fun equals(other: Any?) = other is PowerBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "power".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryPowerSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = PowerBinarySensor(kHassInstance),
        deviceClass = "power"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryPowerSensorState.parseState(it) }


        fun onPowerDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(POWER, callback)

        fun onNoPowerDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(NO_POWER, callback)
    }
}

sealed class BinaryPowerSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryPowerSensorState = when (onOff) {
            OnOff.ON -> POWER
            OnOff.OFF -> NO_POWER
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Power is detected */
    object POWER : BinaryPowerSensorState(OnOff.ON)

    /** No power is detected */
    object NO_POWER : BinaryPowerSensorState(OnOff.OFF)
}

val KHomeAssistant.PowerBinarySensor: PowerBinarySensor
    get() = PowerBinarySensor(this)