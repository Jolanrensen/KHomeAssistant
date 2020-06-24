package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySoundSensorState.SOUND
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySoundSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class SoundBinarySensor(kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinarySoundSensorState, SoundBinarySensor.Entity>(kHassInstance) {

    /** Making sure SoundSensor acts as a singleton. */
    override fun equals(other: Any?) = other is SoundBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "sound".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinarySoundSensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = SoundBinarySensor(kHassInstance),
        deviceClass = "sound"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinarySoundSensorState.parseState(it) }


        fun onSoundDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(SOUND, callback)

        fun onTurnedClear(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLEAR, callback)
    }
}

sealed class BinarySoundSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinarySoundSensorState = when (onOff) {
            OnOff.ON -> SOUND
            OnOff.OFF -> CLEAR
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Sound is detected */
    object SOUND : BinarySoundSensorState(OnOff.ON)

    /** No sound is detected */
    object CLEAR : BinarySoundSensorState(OnOff.OFF)
}

val KHomeAssistant.SoundBinarySensor: SoundBinarySensor
    get() = SoundBinarySensor(this)