package nl.jolanrensen.kHomeAssistant

import kotlin.browser.window

actual object Clock {
    actual fun fixedRateTimer(rate: Long, action: () -> Unit) {
        window.setInterval({ action() }, rate.toInt())
    } // TODO maybe running in Node.js?
}