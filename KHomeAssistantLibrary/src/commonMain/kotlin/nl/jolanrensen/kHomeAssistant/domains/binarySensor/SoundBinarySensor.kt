package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySoundSensorState.SOUND
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySoundSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class SoundBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinarySoundSensorState, SoundBinarySensor.Entity>() {

    /** Making sure SoundSensor acts as a singleton. */
    override fun equals(other: Any?) = other is SoundBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "sound".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinarySoundSensorState>(
        getKHass = getKHass,
        name = name,
        domain = SoundBinarySensor(getKHass),
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

val HasKHassContext.SoundBinarySensor: SoundBinarySensor
    get() = SoundBinarySensor(getKHass)