package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySmokeSensorState.SMOKE
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySmokeSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class SmokeBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinarySmokeSensorState, SmokeBinarySensor.Entity>() {

    /** Making sure SmokeSensor acts as a singleton. */
    override fun equals(other: Any?) = other is SmokeBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "smoke".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinarySmokeSensorState>(
        getKHass = getKHass,
        name = name,
        domain = SmokeBinarySensor(getKHass),
        deviceClass = "smoke"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinarySmokeSensorState.parseState(it) }


        fun onSmokeDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(SMOKE, callback)

        fun onTurnedClear(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLEAR, callback)
    }
}

sealed class BinarySmokeSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinarySmokeSensorState = when (onOff) {
            OnOff.ON -> SMOKE
            OnOff.OFF -> CLEAR
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Smoke is detected */
    object SMOKE : BinarySmokeSensorState(OnOff.ON)

    /** No smoke is detected */
    object CLEAR : BinarySmokeSensorState(OnOff.OFF)
}

val HasKHassContext.SmokeBinarySensor: SmokeBinarySensor
    get() = SmokeBinarySensor(getKHass)