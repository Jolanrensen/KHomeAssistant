package nl.jolanrensen.kHomeAssistant

import kotlin.concurrent.fixedRateTimer

actual object Clock {
    actual fun fixedRateTimer(rate: Long, action: () -> Unit) {
        fixedRateTimer(period = rate) {
            action()
        }
    }
}