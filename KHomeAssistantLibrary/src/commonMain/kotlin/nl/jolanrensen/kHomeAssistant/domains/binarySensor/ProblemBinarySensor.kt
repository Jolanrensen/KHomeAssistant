package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryProblemSensorState.PROBLEM
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryProblemSensorState.OK
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class ProblemBinarySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryProblemSensorState, ProblemBinarySensor.Entity>() {

    /** Making sure ProblemSensor acts as a singleton. */
    override fun equals(other: Any?) = other is ProblemBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "problem".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryProblemSensorState>(
        getKHass = getKHass,
        name = name,
        domain = ProblemBinarySensor(getKHass),
        deviceClass = "problem"
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryProblemSensorState.parseState(it) }


        fun onProblemDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(PROBLEM, callback)

        fun onNoProblemDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(OK, callback)
    }
}

sealed class BinaryProblemSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryProblemSensorState = when (onOff) {
            OnOff.ON -> PROBLEM
            OnOff.OFF -> OK
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Problem is detected */
    object PROBLEM : BinaryProblemSensorState(OnOff.ON)

    /** No problem is detected */
    object OK : BinaryProblemSensorState(OnOff.OFF)
}

val HasKHassContext.ProblemBinarySensor: ProblemBinarySensor
    get() = ProblemBinarySensor(getKHass)