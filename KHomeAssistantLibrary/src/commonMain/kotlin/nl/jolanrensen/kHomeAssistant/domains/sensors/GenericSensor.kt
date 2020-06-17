package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

/** Generic sensor. The type of state will be a String and the unit_of_measurement will be absent. */
class GenericSensor(override var kHomeAssistant: () -> KHomeAssistant?) : AbstractSensor<String, GenericSensor.Entity>() {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericSensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            getKHomeAssistant = kHomeAssistant,
            name = name
        )

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractSensorEntity<String>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = GenericSensor(getKHomeAssistant),
        deviceClass = null
    ) {
        override fun parseStateValue(stateValue: String) = stateValue
        override fun getStateValue(state: String) = state
    }
}


/** Access the GenericSensor Domain */
val HasContext.GenericSensor: GenericSensor
    get() = GenericSensor(getKHomeAssistant)