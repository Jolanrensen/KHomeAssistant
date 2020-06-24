package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity

abstract class AbstractSensor<StateType : Any, EntityType : AbstractSensorEntity<StateType>>
    (kHassInstance: KHomeAssistant) : Domain<EntityType>, KHomeAssistant by kHassInstance {
    override val domainName = "sensor"
}

abstract class AbstractSensorEntity<StateType : Any>(
    kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: AbstractSensor<StateType, out AbstractSensorEntity<StateType>>,
    private val deviceClass: String?
) : BaseEntity<StateType>(
    kHassInstance = kHassInstance,
    name = name,
    domain = domain
) {

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

    init {
        attributes += arrayOf(
            ::device_class,
            ::readable_state,
            ::deviceClass // TODO REMOVE!
        )
    }

    /** The class of the device as set by configuration, changing the device state and icon that is displayed on the UI (see below). It does not set the unit_of_measurement.*/
//    val device_class: String by attrsDelegate()

    /** Readable state with added unit of measurement, aka '50%'. */
    val readable_state: String
        get() = unit_of_measurement?.let { "$state$unit_of_measurement" } ?: state.toString()

}





