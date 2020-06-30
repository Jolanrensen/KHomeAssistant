package examples

import nl.jolanrensen.kHomeAssistant.Automation
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.core.onEventFired

class Monitor(private val events: Iterable<String>, kHass: KHomeAssistant) : Automation(kHass) {

    override suspend fun initialize() {
        events.forEach {
            onEventFired(it) {
                println(it)
            }
        }
    }
}