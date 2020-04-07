package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan

object Scheduler {

    fun KHomeAssistantContext.runEvery(timeSpan: TimeSpan, startingAt: DateTimeTz, callback: suspend () -> Unit) = runEvery(timeSpan, startingAt.utc, callback)

    fun KHomeAssistantContext.runEvery(timeSpan: TimeSpan, startingAt: DateTime = DateTime.EPOCH, callback: suspend () -> Unit) {
        kHomeAssistant()!!.scheduledRepeatedTasks += RepeatedTask(timeSpan, startingAt, callback)
    }


}

data class RepeatedTask(
    val runEvery: TimeSpan,
    var startingAt: DateTime, // can be adjusted to a startingAt closest to now, yet in the past
    val callback: suspend () -> Unit
)