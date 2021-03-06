package examples

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Notify
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.runEveryDayAt
import java.util.HashSet

class Battery(private val threshold: Int, private val alwaysSend: Boolean = false, kHass: KHomeAssistant) : Automation(kHass) {

    override suspend fun initialize() {
        runEveryDayAt(hour = 6) {
            val batteryDevices: HashSet<Pair<DefaultEntity, Int>> = hashSetOf()

            for (device in entities) {
                val battery = device["battery"]?.jsonPrimitive?.intOrNull
                    ?: device["battery_level"]?.jsonPrimitive?.intOrNull
                    ?: if (device["device_class"]?.jsonPrimitive?.contentOrNull == "battery")
                        device.state.toIntOrNull()
                    else null

                if (battery != null) {
                    batteryDevices += device to battery
                }
            }

            val lowDevices: List<Pair<DefaultEntity, Int>> = batteryDevices.filter { it.second < threshold }

            var message = "Battery Level Report\n\n"

            if (lowDevices.isNotEmpty()) {
                message += """The following devices are low:
                    |${lowDevices.joinToString("\n") { it.first.entityID }}
                    |""".trimMargin()
            }

            message += """Battery Levels:
                |${batteryDevices.joinToString("\n") { "${it.first.entityID}: ${it.second}%" }}
                |""".trimMargin()

            if (lowDevices.isNotEmpty() || alwaysSend) {
                Notify.notify(title = "Home Assistant Battery Report", message = message)
            }
        }
    }
}