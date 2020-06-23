package nl.jolanrensen.kHomeAssistant.core

import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.messages.Event

fun HasKHassContext.onEventFired(eventType: String, callback: suspend (Event) -> Unit) {
    getKHass()!!
            .eventListeners
            .getOrPut(eventType) { hashSetOf() }
            .add(callback)
}