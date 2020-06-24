package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryLockSensorState.UNLOCKED
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryLockSensorState.LOCKED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class LockBinarySensor(kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryLockSensorState, LockBinarySensor.Entity>(kHassInstance) {

    /** Making sure LockSensor acts as a singleton. */
    override fun equals(other: Any?) = other is LockBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "lock".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryLockSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = LockBinarySensor(kHassInstance),
        deviceClass = "lock"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryLockSensorState.parseState(it) }


        fun onUnlocked(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(UNLOCKED, callback)

        fun onLocked(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(LOCKED, callback)
    }
}

sealed class BinaryLockSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryLockSensorState = when (onOff) {
            OnOff.ON -> UNLOCKED
            OnOff.OFF -> LOCKED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Lock is opened */
    object UNLOCKED : BinaryLockSensorState(OnOff.ON)

    /** Lock is closed */
    object LOCKED : BinaryLockSensorState(OnOff.OFF)
}

val KHomeAssistant.LockBinarySensor: LockBinarySensor
    get() = LockBinarySensor(this)