package nl.jolanrensen.kHomeAssistant

import kotlin.browser.window

actual object Clock {
    actual fun fixedRateTimer(rate: Long, action: () -> Unit) {
        val timer = window.setInterval({ action() }, rate.toInt())
    } // TODO maybe running in Node.js?

    //TODO

    actual fun cancelAllTimers() {
        while (timers.isNotEmpty()) {
            timers.iterator().apply {
                next().cancel()
                remove()
            }
        }
    }
}