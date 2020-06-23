package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

/** Generic sensor. The type of state will be a String and the unit_of_measurement will be absent. */
class GenericSensor(override var getKHass: () -> KHomeAssistant?) : AbstractSensor<String, GenericSensor.Entity>() {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericSensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            getKHass = getKHass,
            name = name
        )

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractSensorEntity<String>(
        getKHass = getKHass,
        name = name,
        domain = GenericSensor(getKHass),
        deviceClass = null
    ) {
        override fun stringToState(stateValue: String) = stateValue
        override fun stateToString(state: String) = state
    }
}


/** Access the GenericSensor Domain */
val HasKHassContext.GenericSensor: GenericSensor
    get() = GenericSensor(getKHass)