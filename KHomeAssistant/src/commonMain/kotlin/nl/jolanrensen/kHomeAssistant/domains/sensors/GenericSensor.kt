package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.domains.withContext

/** Generic sensor. The type of state will be a String and the unit_of_measurement will be absent. */
object GenericSensor : AbstractSensor<String, GenericSensor.Entity>() {

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
        domain = GenericSensor,
        deviceClass = null
    ) {
        override fun parseStateValue(stateValue: String) = stateValue
        override fun getStateValue(state: String) = state
    }
}

typealias GenericSensorDomain = GenericSensor

/** Access the GenericSensor Domain */
val KHomeAssistantContext.GenericSensor: GenericSensorDomain
    get() = GenericSensorDomain.withContext(kHomeAssistant)