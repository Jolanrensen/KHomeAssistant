package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChangedTo

/** Battery sensor. The type of state will be a Float and the unit_of_measurement will be '%'. */
class BatterySensor(override val kHassInstance: KHomeAssistant) :
    AbstractSensor<Float, BatterySensor.HassAttributes, BatterySensor.Entity>(kHassInstance) {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatterySensor
    override fun hashCode(): Int = domainName.hashCode() + "battery".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            kHassInstance = kHassInstance,
            name = name
        )

    interface HassAttributes : BaseHassAttributes {
        /**  Boolean to indicate whether the device is charging.  */
        val is_charging: Boolean

        /** Can be for instance 'AC', or 'N/A'. */
        val charger_type: String
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractSensorEntity<Float, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = BatterySensor(kHassInstance),
        deviceClass = "battery"
    ), HassAttributes {
        override fun stringToState(stateValue: String) = stateValue.toFloatOrNull()
        override fun stateToString(state: Float) = state.toString()

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        // Attributes
        override val is_charging: Boolean by attrsDelegate()
        override val charger_type: String by attrsDelegate()

        fun onStartedCharging(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::is_charging, true, callback)

        fun onStoppedCharging(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::is_charging, false, callback)
    }
}

val KHomeAssistant.BatterySensor: BatterySensor
    get() = BatterySensor(this)
