package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

class BinaryBatterySensor(override var getKHomeAssistant: () -> KHomeAssistant?) :
    AbstractBinarySensor<BinaryBatterySensorState, BinaryBatterySensor.Entity>() {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BinaryBatterySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery".hashCode()

    override fun Entity(name: String): Entity = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryBatterySensorState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = BinaryBatterySensor(getKHomeAssistant),
        deviceClass = "battery"
    ) {
        override fun parseStateValue(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryBatterySensorState.parseState(it) }
    }
}

sealed class BinaryBatterySensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryBatterySensorState = when (onOff) {
            OnOff.ON -> LOW
            OnOff.OFF -> NORMAL
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Low battery level */
    object LOW : BinaryBatterySensorState(OnOff.ON)

    /** High battery level */
    object NORMAL : BinaryBatterySensorState(OnOff.OFF)
}

val HasContext.BinaryBatterySensor: BinaryBatterySensor
    get() = BinaryBatterySensor(getKHomeAssistant)