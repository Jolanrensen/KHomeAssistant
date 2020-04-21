package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.*
import nl.jolanrensen.kHomeAssistant.domains.sun

/**
 * The scheduler always assumes local time instead of UTC time to give you a piece of mind.
 * You can create a "local time" DateTimeTz by defining the local date/time you want in a normal DateTime object and
 * calling 'yourDateTime.localUnadjusted'. This will for instance make the 9 o'clock you entered in the DateTime object
 * remain 9 o'clock in the DateTimeTz object.
 *
 * If you do want to work with (UTC) DateTime and (local) DateTimeTz, you can convert between the two like this:
 *
 *     'val myLocalTime: DateTimeTz = myUtcDateTime.local'
 * and
 *     'val myUtcTime: DateTime = myLocalDateTimeTz.utc'
 */


// TODO
//fun KHomeAssistantContext.runEveryWeekAt(dayOfWeek: DayOfWeek, time: Time, callback: suspend () -> Unit): Task {
//    val offsetAtEpoch = DateTime.EPOCH.localUnadjusted.offset.time
//    return runEvery(1.weeks, DateTime(DateTime.EPOCH.date, time).localUnadjusted - offsetAtEpoch, callback)
//}

/** Schedule something to execute at a certain (local) time each day. */
fun KHomeAssistantContext.runEveryDayAt(
    hour: Int,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit
) = runEveryDayAt(Time(hour, minute, second, millisecond), callback)

/** Schedule something to execute at a certain (local) time each day. */
fun KHomeAssistantContext.runEveryDayAt(time: Time, callback: suspend () -> Unit): Task {
    val offsetAtEpoch = DateTime.EPOCH.localUnadjusted.offset.time
    return runEvery(1.days, DateTime(DateTime.EPOCH.date, time).localUnadjusted - offsetAtEpoch, callback)
}

/** Schedule something to execute each week, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute, hour, day, week etc. will be picked. */
fun KHomeAssistantContext.runEveryWeek(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.weeks, alignWith, callback)

/** Schedule something to execute each day, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute, hour, day, week etc. will be picked. */
fun KHomeAssistantContext.runEveryDay(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.days, alignWith, callback)

/** Schedule something to execute each hour, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute, hour, day, week etc. will be picked. */
fun KHomeAssistantContext.runEveryHour(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.hours, alignWith, callback)

/** Schedule something to execute each minute, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute, hour, day, week etc. will be picked. */
fun KHomeAssistantContext.runEveryMinute(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.minutes, alignWith, callback)

/** Schedule something to repeatedly execute each given timespan, optionally aligned with a certain point in (local) time. If not aligned, the local epoch will be picked. */
fun KHomeAssistantContext.runEvery(
    timeSpan: TimeSpan,
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedTask(timeSpan, alignWith.utc, callback)

    kHomeAssistant()!!.scheduledRepeatedTasks += task

    return object : Task {
        override fun cancel() {
            try {
                kHomeAssistant()!!.scheduledRepeatedTasks -= task
            } catch (e: Exception) {
            }
        }
    }
}

/** Schedule something to execute each day at sunrise. */
fun KHomeAssistantContext.runEveryDayAtSunrise(callback: suspend () -> Unit) =
    runAt({ sun.next_rising.local }, callback)

/** Schedule something to execute each day at sunset. */
fun KHomeAssistantContext.runEveryDayAtSunset(callback: suspend () -> Unit) =
    runAt({ sun.next_setting.local }, callback)

/** Schedule something to execute each day at dawn. */
fun KHomeAssistantContext.runEveryDayAtDawn(callback: suspend () -> Unit) =
    runAt({ sun.next_dawn.local }, callback)

/** Schedule something to execute each day at dusk. */
fun KHomeAssistantContext.runEveryDayAtDusk(callback: suspend () -> Unit) =
    runAt({ sun.next_dusk.local }, callback)

/** Schedule something to execute each day at noon. */
fun KHomeAssistantContext.runEveryDayAtNoon(callback: suspend () -> Unit) =
    runAt({ sun.next_noon.local }, callback)

/** Schedule something to execute each day at midnight. */
fun KHomeAssistantContext.runEveryDayAtMidnight(callback: suspend () -> Unit) =
    runAt({ sun.next_midnight.local }, callback)

/** Schedule something to execute at a given point in (local) time. The task will automatically be canceled after execution. */
fun KHomeAssistantContext.runAt(
    dateTimeTz: DateTimeTz,
    callback: suspend () -> Unit
): Task {
    var cancel: (() -> Unit)? = null
    val task = runAt({ dateTimeTz }) {
        callback()
        cancel!!.invoke()
    }
    cancel = task::cancel

    return task
}

/** Schedule something to run at a point in (local) time that can be obtained using the given getDateTimeTz callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI. */
fun KHomeAssistantContext.runAt(
    getDateTimeTz: () -> DateTimeTz,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedIrregularTask(
        currentNextTime = getDateTimeTz().utc,
        getNextTime = { getDateTimeTz().utc },
        callback = callback
    )

    kHomeAssistant()!!.scheduledRepeatedIrregularTasks += task

    return object : Task {
        override fun cancel() {
            try {
                kHomeAssistant()!!.scheduledRepeatedIrregularTasks -= task
            } catch (e: Exception) {
            }
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

data class RepeatedIrregularTask(
    var currentNextTime: DateTime,
    val getNextTime: () -> DateTime,
    val callback: suspend () -> Unit
)