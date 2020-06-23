package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChangedTo

/** Battery sensor. The type of state will be a Float and the unit_of_measurement will be '%'. */
class BatterySensor(override var getKHass: () -> KHomeAssistant?) :
    AbstractSensor<Float, BatterySensor.Entity>() {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatterySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            getKHass = getKHass,
            name = name
        )

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractSensorEntity<Float>(
        getKHass = getKHass,
        name = name,
        domain = BatterySensor(getKHass),
        deviceClass = "battery"
    ) {
        override fun stringToState(stateValue: String) = stateValue.toFloatOrNull()
        override fun stateToString(state: Float) = state.toString()

        init {
            attributes += arrayOf(::is_charging, ::charger_type)
        }

        /**  Boolean to indicate whether the device is charging.  */
        val is_charging: Boolean by attrsDelegate()

        /** Can be for instance 'AC', or 'N/A'. */
        val charger_type: String by attrsDelegate()

        fun onStartedCharging(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::is_charging, true, callback)

        fun onStoppedCharging(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::is_charging, false, callback)
    }
}

val HasKHassContext.BatterySensor: BatterySensor
    get() = BatterySensor(getKHass)
