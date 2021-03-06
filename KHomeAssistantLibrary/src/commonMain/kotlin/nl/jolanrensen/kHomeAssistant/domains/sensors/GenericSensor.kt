package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes

/** Generic sensor. The type of state will be a String and the unit_of_measurement will be absent. */
class GenericSensor(override val kHassInstance: KHomeAssistant) :
    AbstractSensor<String, GenericSensor.HassAttributes, GenericSensor.Entity>(kHassInstance) {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericSensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BinarySensorHassAttributes


    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractSensorEntity<String, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = GenericSensor(kHassInstance),
        expectedDeviceClass = SensorDeviceClass.GENERIC
    ), HassAttributes {

        override val unit_of_measurement: String? by attrsDelegate(null)

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<BaseHassAttributes>()

        override fun stringToState(stateValue: String) = stateValue
        override fun stateToString(state: String) = state
    }
}


/** Access the GenericSensor Domain */
val KHomeAssistant.GenericSensor: GenericSensor
    get() = GenericSensor(this)