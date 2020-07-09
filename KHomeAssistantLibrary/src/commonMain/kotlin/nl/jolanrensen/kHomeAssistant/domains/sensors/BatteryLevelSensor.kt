package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChangedTo

/** Battery Level sensor. The type of state will be a Float and the unit_of_measurement will be '%'. */
class BatteryLevelSensor(override val kHassInstance: KHomeAssistant) :
    AbstractSensor<Float, BatteryLevelSensor.HassAttributes, BatteryLevelSensor.Entity>(kHassInstance) {

    /** Making sure BatterySensor acts as a singleton. */
    override fun equals(other: Any?) = other is BatteryLevelSensor
    override fun hashCode(): Int = domainName.hashCode() + "battery_level".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BinarySensorHassAttributes

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractSensorEntity<Float, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = BatteryLevelSensor(kHassInstance),
        expectedDeviceClass = SensorDeviceClass.BATTERY
    ), HassAttributes {
        override fun stringToState(stateValue: String) = stateValue.toFloatOrNull()
        override fun stateToString(state: Float) = state.toString()

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        // Attributes
        override val unit_of_measurement: String? by attrsDelegate("%")
    }
}

val KHomeAssistant.BatteryLevelSensor: BatteryLevelSensor
    get() = BatteryLevelSensor(this)
