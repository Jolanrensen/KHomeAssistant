package nl.jolanrensen.kHomeAssistant.domains.binarySensor

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryConnectivitySensorState.CONNECTED
import nl.jolanrensen.kHomeAssistant.domains.binarySensor.BinaryConnectivitySensorState.DISCONNECTED
import nl.jolanrensen.kHomeAssistant.entities.onStateChangedTo

class ConnectivityBinarySensor(override val kHassInstance: KHomeAssistant) :
    AbstractBinarySensor<BinaryConnectivitySensorState, ConnectivityBinarySensor.Entity>(kHassInstance) {

    /** Making sure ConnectivitySensor acts as a singleton. */
    override fun equals(other: Any?) = other is ConnectivityBinarySensor
    override fun hashCode(): Int = domainName.hashCode() + "connectivity".hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : AbstractBinarySensorEntity<BinaryConnectivitySensorState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = ConnectivityBinarySensor(kHassInstance),
        expectedDeviceClass = BinarySensorDeviceClass.CONNECTIVITY
    ) {
        override fun stringToState(stateValue: String) =
            OnOff.values()
                .find { it.stateValue == stateValue }
                ?.let { BinaryConnectivitySensorState.parseState(it) }


        fun onConnected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(CONNECTED, callback)

        fun onDisconnected(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedTo(DISCONNECTED, callback)
    }
}

sealed class BinaryConnectivitySensorState(override val onOffValue: OnOff) : DeviceClassState() {
    companion object {
        fun parseState(onOff: OnOff): BinaryConnectivitySensorState = when (onOff) {
            OnOff.ON -> CONNECTED
            OnOff.OFF -> DISCONNECTED
            else -> throw IllegalArgumentException("$onOff is not supported")
        }
    }

    /** Connected */
    object CONNECTED : BinaryConnectivitySensorState(OnOff.ON)

    /** Disconnected */
    object DISCONNECTED : BinaryConnectivitySensorState(OnOff.OFF)
}

val KHomeAssistant.ConnectivityBinarySensor: ConnectivityBinarySensor
    get() = ConnectivityBinarySensor(this)