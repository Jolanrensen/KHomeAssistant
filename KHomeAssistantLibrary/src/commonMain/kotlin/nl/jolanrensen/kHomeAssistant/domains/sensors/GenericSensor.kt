package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant

/** Generic sensor. The type of state will be a String and the unit_of_measurement will be absent. */
class GenericSensor(kHassInstance: KHomeAssistant) : AbstractSensor<String, GenericSensor.Entity>(kHassInstance) {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericSensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            kHassInstance = this,
            name = name
        )

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractSensorEntity<String>(
        kHassInstance = kHassInstance,
        name = name,
        domain = GenericSensor(kHassInstance),
        deviceClass = null
    ) {
        override fun stringToState(stateValue: String) = stateValue
        override fun stateToString(state: String) = state
    }
}


/** Access the GenericSensor Domain */
val KHomeAssistant.GenericSensor: GenericSensor
    get() = GenericSensor(this)