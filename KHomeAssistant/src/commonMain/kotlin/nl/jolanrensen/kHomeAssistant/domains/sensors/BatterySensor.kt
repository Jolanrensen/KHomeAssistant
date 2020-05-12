package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

/** Battery sensor. The type of state will be a Float and the unit_of_measurement will be '%'. */
class BatterySensor(override var kHomeAssistant: () -> KHomeAssistant?) : AbstractSensor<Float, BatterySensor.Entity>() {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatterySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            kHomeAssistant = kHomeAssistant,
            name = name
        )

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractSensorEntity<Float>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = BatterySensor(kHomeAssistant),
        deviceClass = "battery"
    ) {
        override fun parseStateValue(stateValue: String) = stateValue.toFloatOrNull()
        override fun getStateValue(state: Float) = state.toString()

        init {
            attributes += arrayOf(::is_charging, ::charger_type)
        }

        /**  Boolean to indicate whether the device is charging.  */
        val is_charging: Boolean? by attrsDelegate

        /** Can be for instance 'AC', or 'N/A'. */
        val charger_type: String? by attrsDelegate
    }
}

val KHomeAssistantContext.BatterySensor: BatterySensor
    get() = BatterySensor(kHomeAssistant)
