package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.*

fun KHomeAssistantContext.runEveryDayAt(
    hour: Int,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit
) = runEveryDayAt(Time(hour, minute, second, millisecond), callback)

fun KHomeAssistantContext.runEveryDayAt(time: Time, callback: suspend () -> Unit): Task {
    val offsetAtEpoch = DateTime.EPOCH.localUnadjusted.offset.time
    return runEvery(1.days, DateTime(DateTime.EPOCH.date, time).localUnadjusted - offsetAtEpoch, callback)
}

fun KHomeAssistantContext.runEveryDay(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.days, alignWith, callback)

fun KHomeAssistantContext.runEveryHour(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.hours, alignWith, callback)


fun KHomeAssistantContext.runEveryMinute(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.minutes, alignWith, callback)

fun KHomeAssistantContext.runEvery(
    runEvery: TimeSpan,
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedTask(runEvery, alignWith.utc, callback)
    kHomeAssistant()!!.scheduledRepeatedTasks += task

    return object : Task {
        override fun cancel() {
            try {
                kHomeAssistant()!!.scheduledRepeatedTasks -= task
            } catch (e: Exception) {}
        }
    }
}

interface Task {
    fun cancel()
}

data class RepeatedTask(
    val runEvery: TimeSpan,
    var alignWith: DateTime, // can be adjusted to a alignWith closest to now, yet in the past
    val callback: suspend () -> Unit
)