package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity

abstract class AbstractSensor<StateType : Any, E : AbstractSensorEntity<StateType>> : Domain<E> {

    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "sensor"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'XXXSensor.' from a KHomeAssistantContext instead of using XXXSensor directly.""".trimMargin()
    }
}

abstract class AbstractSensorEntity<StateType : Any>(
    override val kHomeAssistant: () -> KHomeAssistant?,
    override val name: String,
    override val domain: AbstractSensor<StateType, out AbstractSensorEntity<StateType>>,
    private val deviceClass: String?
) : BaseEntity<StateType>(
    kHomeAssistant = kHomeAssistant,
    name = name,
    domain = domain
) {

    private var isCorrectDevice = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun checkEntityExists() {
        if (!isCorrectDevice && kHomeAssistant?.invoke() != null) {
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
    val device_class: String by attrsDelegate()

    /** Readable state with added unit of measurement, aka '50%'. */
    val readable_state: String
        get() = unit_of_measurement?.let { "$state$unit_of_measurement" } ?: state.toString()

}





