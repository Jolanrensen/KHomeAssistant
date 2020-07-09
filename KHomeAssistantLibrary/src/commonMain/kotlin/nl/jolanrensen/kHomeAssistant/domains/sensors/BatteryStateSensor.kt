package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChangedTo

/** Battery Level sensor. The type of state will be of type [BatteryStateSensor.State] and the unit_of_measurement will be absent. */
class BatteryStateSensor(override val kHassInstance: KHomeAssistant) :
    AbstractSensor<BatteryStateSensor.State, BatteryStateSensor.HassAttributes, BatteryStateSensor.Entity>(kHassInstance) {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatteryStateSensor
    override fun hashCode(): Int = domainName.hashCode() + "battery_state".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    enum class State(val value: String?) {
        DISCHARGING("discharging"),
        CHARGING("charging"),
        NOT_CHARGING("not_charging"),
        UNKNOWN(null)
    }

    interface HassAttributes : BinarySensorHassAttributes {
        /**  Boolean to indicate whether the device is charging.  */
        val is_charging: Boolean

        /** Can be for instance 'AC', 'usb', or 'N/A'. */
        val charger_type: String
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractSensorEntity<State, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = BatteryStateSensor(kHassInstance),
        expectedDeviceClass = SensorDeviceClass.BATTERY
    ), HassAttributes {
        override fun stringToState(stateValue: String) = State.values().find { it.value == stateValue } ?: State.UNKNOWN
        override fun stateToString(state: State) = state.toString()

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        // Attributes
        override val is_charging: Boolean by attrsDelegate()
        override val charger_type: String by attrsDelegate()
        override val unit_of_measurement: String? by attrsDelegate(null)

        fun onStartedCharging(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::is_charging, true, callback)

        fun onStoppedCharging(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::is_charging, false, callback)
    }
}

val KHomeAssistant.BatteryStateSensor: BatteryStateSensor
    get() = BatteryStateSensor(this)
