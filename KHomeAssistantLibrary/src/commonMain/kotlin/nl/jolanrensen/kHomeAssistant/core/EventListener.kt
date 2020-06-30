package nl.jolanrensen.kHomeAssistant.core

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.messages.Event

fun KHomeAssistant.onEventFired(eventType: String, callback: suspend (Event) -> Unit) {
    eventListeners
        .getOrPut(eventType) { hashSetOf() }
        .add(callback)
}