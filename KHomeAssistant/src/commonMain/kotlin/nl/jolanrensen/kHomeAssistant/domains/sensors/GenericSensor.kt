package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

/** Generic sensor. The type of state will be a String and the unit_of_measurement will be absent. */
class GenericSensor(override var kHomeAssistant: () -> KHomeAssistant?) : AbstractSensor<String, GenericSensor.Entity>() {

    /** Making sure GenericSensor acts as a singleton. */
    override fun equals(other: Any?) = other is GenericSensor
    override fun hashCode(): Int = domainName.hashCode() + "generic".hashCode()

    override fun Entity(name: String): Entity =
        Entity(
            kHomeAssistant = kHomeAssistant,
            name = name
        )

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : AbstractSensorEntity<String>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = GenericSensor(kHomeAssistant),
        deviceClass = null
    ) {
        override fun parseStateValue(stateValue: String) = stateValue
        override fun getStateValue(state: String) = state
    }
}


/** Access the GenericSensor Domain */
val KHomeAssistantContext.GenericSensor: GenericSensor
    get() = GenericSensor(kHomeAssistant)