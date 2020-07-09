package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.entities.*

/**
 * https://www.home-assistant.io/integrations/binary_sensor/
 * Not to be confused with the "sensor" domain.
 * A "binary_sensor" is a completely different entity type.
 */
abstract class AbstractBinarySensor
<StateType : DeviceClassState, EntityType : AbstractBinarySensorEntity<StateType>>
    (override val kHassInstance: KHomeAssistant) : Domain<EntityType> {
    override val domainName = "binary_sensor"

    /** Making sure AbstractBinarySensor acts as a singleton. */
    override fun equals(other: Any?) = other is AbstractBinarySensor<*, *>
    override fun hashCode(): Int = domainName.hashCode()
}

enum class BinarySensorDeviceClass(val value: String?) {
    GENERIC(null),
    BATTERY("battery"),
    BATTERY_CHARGING("battery_charging"),
    COLD("cold"),
    CONNECTIVITY("connectivity"),
    DOOR("door"),
    GARAGE_DOOR("garage_door"),
    GAS("gas"),
    HEAT("heat"),
    LIGHT("light"),
    LOCK("lock"),
    MOISTURE("moisture"),
    MOTION("motion"),
    MOVING("moving"),
    OCCUPANCY("occupancy"),
    OPENING("opening"),
    PLUG("plug"),
    POWER("power"),
    PRESENCE("presence"),
    PROBLEM("problem"),
    SAFETY("safety"),
    SMOKE("smoke"),
    SOUND("sound"),
    VIBRATION("vibration"),
    WINDOW("window")
}


interface BinarySensorHassAttributes : BaseHassAttributes {
    // Read only

    /** The class of the device as set by configuration. @see [deviceClass]. */
    @Deprecated("You can use the typed version", replaceWith = ReplaceWith("deviceClass"))
    val device_class: String?

    // Helper
    val deviceClass: BinarySensorDeviceClass
        get() = BinarySensorDeviceClass.values()
            .find { it.value == device_class } ?: BinarySensorDeviceClass.GENERIC

}

abstract class AbstractBinarySensorEntity<StateType : DeviceClassState>(
    override val kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: AbstractBinarySensor<StateType, out AbstractBinarySensorEntity<StateType>>,
    private val expectedDeviceClass: BinarySensorDeviceClass
) : BaseEntity<StateType, BinarySensorHassAttributes>(
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

    override fun stateToString(state: StateType): String = state.onOffValue.stateValue

    override val hassAttributes: Array<Attribute<*>> = getHassAttributes<BinarySensorHassAttributes>()

    override val additionalToStringAttributes: Array<Attribute<*>> =
        super.additionalToStringAttributes + ::expectedDeviceClass + getHassAttributesHelpers<BinarySensorHassAttributes>()
}

abstract class DeviceClassState {
    abstract val onOffValue: OnOff
}
