package examples

import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Notify
import nl.jolanrensen.kHomeAssistant.domains.sensors.BatteryLevelSensor
import nl.jolanrensen.kHomeAssistant.domains.sensors.BatteryStateSensor
import nl.jolanrensen.kHomeAssistant.entities.onStateChanged

class BatteryBelowThreshold(nameOfBatterySensor: String, private val threshold: Int, kHass: KHomeAssistant) :
    Automation(kHass) {

    private var belowThreshold = false
    private val batteryLevelSensor: BatteryLevelSensor.Entity = BatteryLevelSensor[nameOfBatterySensor]

    override suspend fun initialize() {
        batteryLevelSensor.onStateChanged { checkState(this) }

        // initial check
        checkState(batteryLevelSensor)
    }

    private suspend fun checkState(sensor: BatteryLevelSensor.Entity) {
        if (!belowThreshold && sensor.state < threshold) {
            Notify.notify(message = "Battery sensor ${sensor.name}'s percentage is below $threshold.")
        }
        belowThreshold = sensor.state < threshold
    }
}

// functional variant
fun batteryBelowThreshold(kHass: KHomeAssistant, nameOfBatterySensor: String, threshold: Int): Automation =
    automation(kHass, "BatteryBelowThreshold") {
        var belowThreshold = false
        val batteryLevelSensor: BatteryLevelSensor.Entity = BatteryLevelSensor[nameOfBatterySensor]

        val checkState: suspend BatteryLevelSensor.Entity.() -> Unit = {
            if (!belowThreshold && state < threshold) {
                Notify.notify(message = "Battery sensor $name's percentage is below $threshold.")
            }
            belowThreshold = state < threshold
        }

        batteryLevelSensor.onStateChanged(callbackWithout = checkState)

        // initial check
        checkState(batteryLevelSensor)
    }