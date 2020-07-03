package examples

import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.automation
import nl.jolanrensen.kHomeAssistant.domains.Notify
import nl.jolanrensen.kHomeAssistant.domains.sensors.BatterySensor
import nl.jolanrensen.kHomeAssistant.entities.onStateChanged

class BatteryBelowThreshold(nameOfBatterySensor: String, private val threshold: Int, kHass: KHomeAssistant) :
    Automation(kHass) {

    private var belowThreshold = false
    private val batterySensor: BatterySensor.Entity = BatterySensor[nameOfBatterySensor]

    override suspend fun initialize() {
        batterySensor.onStateChanged { checkState(this) }

        // initial check
        checkState(batterySensor)
    }

    private suspend fun checkState(sensor: BatterySensor.Entity) {
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
        val batterySensor: BatterySensor.Entity = BatterySensor[nameOfBatterySensor]

        val checkState: suspend BatterySensor.Entity.() -> Unit = {
            if (!belowThreshold && state < threshold) {
                Notify.notify(message = "Battery sensor $name's percentage is below $threshold.")
            }
            belowThreshold = state < threshold
        }

        batterySensor.onStateChanged(callbackWithout = checkState)

        // initial check
        checkState(batterySensor)
    }