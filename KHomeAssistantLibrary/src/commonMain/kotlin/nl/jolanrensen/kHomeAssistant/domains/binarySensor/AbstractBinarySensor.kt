package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity

/**
 * https://www.home-assistant.io/integrations/binary_sensor/
 * Not to be confused with the "sensor" domain.
 * A "binary_sensor" is a completely different entity type.
 */
abstract class AbstractBinarySensor<StateType : DeviceClassState, EntityType : AbstractBinarySensorEntity<StateType>>
    (kHassInstance: KHomeAssistant) : Domain<EntityType>, KHomeAssistant by kHassInstance {
    override val domainName = "binary_sensor"

    /** Making sure AbstractBinarySensor acts as a singleton. */
    override fun equals(other: Any?) = other is AbstractBinarySensor<*, *>
    override fun hashCode(): Int = domainName.hashCode()
}

abstract class AbstractBinarySensorEntity<StateType : DeviceClassState>(
    kHassInstance: KHomeAssistant,
    override val name: String,
    override val domain: AbstractBinarySensor<StateType, out AbstractBinarySensorEntity<StateType>>,
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

    override fun stateToString(state: StateType): String = state.onOffValue.stateValue


    init {
        this.hassAttributes += arrayOf(
            ::device_class
        )
    }


    /** The class of the device as set by configuration, changing the device state and icon that is displayed on the UI (see below). It does not set the unit_of_measurement.*/
//    val device_class: String by attrsDelegate()
}

abstract class DeviceClassState {
    abstract val onOffValue: OnOff
}



//sealed class BatteryCharging : DeviceClassState("battery_charging") {
//    override fun parseState(onOff: OnOff): BatteryCharging = when (onOff) {
//        OnOff.ON -> CHARGING
//        OnOff.OFF -> NOT_CHARGING
//        else -> throw IllegalArgumentException("$onOff is not supported")
//    }
//
//    object CHARGING : BatteryCharging() {
//        override fun toOnOff() = OnOff.ON
//    }
//
//    object NOT_CHARGING : BatteryCharging() {
//        override fun toOnOff() = OnOff.OFF
//    }
//}
//
//val <D : AbstractBinarySensor.DeviceClassState> HasContext.BinarySensor: AbstractBinarySensor<D>
//    get() = AbstractBinarySensor<D>(getKHomeAssistant)