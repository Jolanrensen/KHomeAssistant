package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGasSensorState.GAS
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryGasSensorState.CLEAR
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class GasBinarySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryGasSensorState, GasBinarySensor.Entity>() {

    /** Making sure GasSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GasBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "gas".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryGasSensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = GasBinarySensor(getKHomeAssistant),
        deviceClass = "gas"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryGasSensorState.parseState(it) }


        fun onGasDetected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(GAS, callback)

        fun onTurnedClear(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CLEAR, callback)
    }
}

sealed class BinaryGasSensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryGasSensorState = when (onOff) {
            OnOff.ON -> GAS
            OnOff.OFF -> CLEAR
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Gas is detected */
    object GAS : BinaryGasSensorState(OnOff.ON)

    /** No gas is detected */
    object CLEAR : BinaryGasSensorState(OnOff.OFF)
}

val HasKHassContext.GasBinarySensor: GasBinarySensor
    get() = GasBinarySensor(getKHomeAssistant)