package nl.jolanrensen.kHomeAssistant.domains.sensors

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinarySensorDeviceClass
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes

abstract class AbstractSensor
<StateType : Any, AttrsType : BinarySensorHassAttributes, EntityType : AbstractSensorEntity<StateType, AttrsType>>
    (override val kHassInstance: KHomeAssistant) : Domain<EntityType> {
    override val domainName = "sensor"
}

enum class SensorDeviceClass(val value: String?) {
    GENERIC(null),
    BATTERY("battery"),
    HUMIDITY("humidity"),
    ILLUMINANCE("illuminance"),
    SIGNAL_STRENGTH("signal_strength"),
    TEMPERATURE("temperature"),
    POWER("power"),
    PRESSURE("pressure"),
    TIMESTAMP("timestamp")
}

interface BinarySensorHassAttributes : BaseHassAttributes {
    // Read only

    /** The class of the device as set by configuration. @see [deviceClass]. */
    @Deprecated("You can use the typed version", replaceWith = ReplaceWith("deviceClass"))
    val device_class: String?

    // Helper
    val deviceClass: SensorDeviceClass
        get() = SensorDeviceClass.values()
            .find { it.value == device_class } ?: SensorDeviceClass.GENERIC

}

abstract class AbstractSensorEntity<StateType : Any, AttrsType : BinarySensorHassAttributes>(
    override val kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: AbstractSensor<StateType, AttrsType, out AbstractSensorEntity<StateType, AttrsType>>,
    private val expectedDeviceClass: SensorDeviceClass
) : BaseEntity<StateType, AttrsType>(
    kHassInstance = kHassInstance,
    name = name,
    domain = domain
), BinarySensorHassAttributes {

    private var isCorrectDevice = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun checkEntityExists() {
        if (!isCorrectDevice) {
            if (expectedDeviceClass != deviceClass)
                throw IllegalArgumentException("It appears the sensor $name is a $deviceClass while you are using a $expectedDeviceClass")
            isCorrectDevice = true

        }
        super.checkEntityExists()
    }

    @Deprecated("You can use the typed version", replaceWith = ReplaceWith("deviceClass"))
    override val device_class: String? by attrsDelegate(null)

    override val additionalToStringAttributes: Array<Attribute<*>> =
        super.additionalToStringAttributes + ::expectedDeviceClass + ::readableState


    /** Readable state with added unit of measurement, aka '50%'. */
    val readableState: String
        get() = unit_of_measurement?.let { "$state$unit_of_measurement" } ?: state.toString()

}





