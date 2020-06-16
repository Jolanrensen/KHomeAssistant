package nl.jolanrensen.kHomeAssistant

import java.util.*
import kotlin.concurrent.fixedRateTimer

actual object Clock {

    private val timers = hashSetOf<Timer>()

    actual fun fixedRateTimer(rate: Long, action: () -> Unit) {
        fixedRateTimer(period = rate) {
            action()
        }.let { timers += it }
    }

    actual fun cancelAllTimers() {
        while (timers.isNotEmpty()) {
            timers.iterator().apply {
                next().cancel()
                remove()
            }
        }
    }
}