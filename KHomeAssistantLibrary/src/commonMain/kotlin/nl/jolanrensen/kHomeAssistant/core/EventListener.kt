package nl.jolanrensen.kHomeAssistant.core

import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.messages.Event

fun HasContext.onEventFired(eventType: String, callback: suspend (Event) -> Unit) {
    getKHomeAssistant()!!
            .eventListeners
            .getOrPut(eventType) { hashSetOf() }
            .add(callback)
}