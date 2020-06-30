package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.HassAttributes

abstract class AbstractSensor
<StateType : Any, AttrsType : BaseHassAttributes, EntityType : AbstractSensorEntity<StateType, AttrsType>>
    (override val kHassInstance: KHomeAssistant) : Domain<EntityType> {
    override val domainName = "sensor"
}

abstract class AbstractSensorEntity<StateType : Any, AttrsType : BaseHassAttributes>(
    override val kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: AbstractSensor<StateType, AttrsType, out AbstractSensorEntity<StateType, AttrsType>>,
    private val deviceClass: String?
) : BaseEntity<StateType, AttrsType>(
    kHassInstance = kHassInstance,
    name = name,
    domain = domain
), BaseHassAttributes {

    private var isCorrectDevice = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun checkEntityExists() {
        if (!isCorrectDevice) {
            if (deviceClass != device_class)
                throw IllegalArgumentException("It appears the sensor $name is a $device_class while you are using a $deviceClass")
            isCorrectDevice = true

        }
        super.checkEntityExists()
    }

    override val additionalToStringAttributes: Array<Attribute<*>> =
        super.additionalToStringAttributes + ::deviceClass + ::readableState


    /** Readable state with added unit of measurement, aka '50%'. */
    val readableState: String
        get() = unit_of_measurement?.let { "$state$unit_of_measurement" } ?: state.toString()

}





