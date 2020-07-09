package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMoistureSensorState.WET
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryMoistureSensorState.DRY
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class MoistureBinarySensor(override val kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryMoistureSensorState, MoistureBinarySensor.Entity>(kHassInstance) {

    /** Making sure MoistureSensor acts as a singleton. */
    override fun equals(other: Any?) = other is MoistureBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "moisture".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryMoistureSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = MoistureBinarySensor(kHassInstance),
        expectedDeviceClass = BinarySensorDeviceClass.MOISTURE
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryMoistureSensorState.parseState(it) }


        fun onMoistureDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(WET, callback)

        fun onTurnedDry(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(DRY, callback)
    }
}

sealed class BinaryMoistureSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryMoistureSensorState = when (onOff) {
            OnOff.ON -> WET
            OnOff.OFF -> DRY
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Moisture is detected */
    object WET : BinaryMoistureSensorState(OnOff.ON)

    /** No moisture is detected */
    object DRY : BinaryMoistureSensorState(OnOff.OFF)
}

val KHomeAssistant.MoistureBinarySensor: MoistureBinarySensor
    get() = MoistureBinarySensor(this)