package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes

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

abstract class AbstractBinarySensorEntity<StateType : DeviceClassState>(
    override val kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: AbstractBinarySensor<StateType, out AbstractBinarySensorEntity<StateType>>,
    private val deviceClass: String?
) : BaseEntity<StateType, BaseHassAttributes>(
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

    override fun stateToString(state: StateType): String = state.onOffValue.stateValue

    override val additionalToStringAttributes: Array<Attribute<*>> =
        super.additionalToStringAttributes + ::deviceClass
}

abstract class DeviceClassState {
    abstract val onOffValue: OnOff
}
