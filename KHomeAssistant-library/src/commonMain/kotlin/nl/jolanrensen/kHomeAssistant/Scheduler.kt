package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.*
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.entities.onChanged

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
suspend fun HasContext.runEveryDayAt(
    hour: Int,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit
) = runEveryDayAt(Time(hour, minute, second, millisecond), callback)

/** Schedule something to execute at a certain (local) time each day. */
suspend fun HasContext.runEveryDayAt(localTime: Time, callback: suspend () -> Unit): Task {
    val offsetAtEpoch = DateTime.EPOCH.localUnadjusted.offset.time
    return runEvery(1.days, DateTime(DateTime.EPOCH.date, localTime).localUnadjusted - offsetAtEpoch, callback)
}

/** Schedule something to execute each week, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the week will be picked. */
suspend fun HasContext.runEveryWeek(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.weeks, alignWith, callback)

/** Schedule something to execute each day, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the day will be picked. */
suspend fun HasContext.runEveryDay(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.days, alignWith, callback)

/** Schedule something to execute each hour, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the hour will be picked. */
suspend fun HasContext.runEveryHour(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.hours, alignWith, callback)

/** Schedule something to execute each minute, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute will be picked. */
suspend fun HasContext.runEveryMinute(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.minutes, alignWith, callback)

/** Schedule something to execute each second, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the second will be picked. */
suspend fun HasContext.runEverySecond(
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
) = runEvery(1.seconds, alignWith, callback)

/** Schedule something to repeatedly execute each given timespan, optionally aligned with a certain point in (local) time. If not aligned, the local epoch (00:00:00 jan 1 1970, local time) will be picked. */
suspend fun HasContext.runEvery(
    timeSpan: TimeSpan,
    alignWith: DateTimeTz = DateTime.EPOCH.localUnadjusted,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedRegularTask(
        kHomeAssistant = getKHomeAssistant()!!,
        alignWith = alignWith.utc,
        runEvery = timeSpan,
        callback = callback
    )

    getKHomeAssistant()!!.schedule(task)

    return object : Task {
        override suspend fun cancel() {
            try { // TODO not sure if try catch necessary
                getKHomeAssistant()!!.cancel(task)
            } catch (e: Exception) {
            }
        }
    }
}

/** Schedule something to execute each day at sunrise. */
suspend fun HasContext.runEveryDayAtSunrise(callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.next_rising.local },
        whenToUpdate = { sun::next_rising.onChanged(sun) { it() } },
        callback = callback
    )

/** Schedule something to execute each day at sunset. */
suspend fun HasContext.runEveryDayAtSunset(callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.next_setting.local },
        whenToUpdate = { sun::next_setting.onChanged(sun) { it() } },
        callback = callback
    )

/** Schedule something to execute each day at dawn. */
suspend fun HasContext.runEveryDayAtDawn(callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.next_dawn.local },
        whenToUpdate = { sun::next_dawn.onChanged(sun) { it() } },
        callback = callback
    )

/** Schedule something to execute each day at dusk. */
suspend fun HasContext.runEveryDayAtDusk(callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.next_dusk.local },
        whenToUpdate = { sun::next_dusk.onChanged(sun) { it() } },
        callback = callback
    )

/** Schedule something to execute each day at noon. */
suspend fun HasContext.runEveryDayAtNoon(callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.next_noon.local },
        whenToUpdate = { sun::next_noon.onChanged(sun) { it() } },
        callback = callback
    )

/** Schedule something to execute each day at midnight. */
suspend fun HasContext.runEveryDayAtMidnight(callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.next_midnight.local },
        whenToUpdate = { sun::next_midnight.onChanged(sun) { it() } },
        callback = callback
    )

/** Schedule something to execute at a given point in (local) time. The task will automatically be canceled after execution. */
suspend fun HasContext.runAt(
    dateTimeTz: DateTimeTz,
    callback: suspend () -> Unit
): Task {
    var cancel: (suspend () -> Unit)? = null
    val task = runAt(
        getNextLocalExecutionTime = { dateTimeTz },
        whenToUpdate = {}
    ) {
        callback()
        cancel!!.invoke()
    }
    cancel = task::cancel

    return task
}

/** Schedule something to run at a point in (local) time that can be obtained using the given getDateTimeTz callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI.
 * ```
 * val task = runAt(
 *    getNextExecutionTime = { someDateTimeTz },
 *    whenToUpdate = { doUpdate -> someEntity.onStateChanged { doUpdate() } }
 * ) {
 *     // do something
 * }
 * ``
 * @receiver the [KHomeAssistantContext] inheriting context (like [Automation]) from which to call it
 * @param getNextExecutionTime a function to get the next execution time in [DateTimeTz]
 * @param whenToUpdate a function providing a `doUpdate` function which should be executed when the value returned at [getNextExecutionTime] has changed
 * @param callback the code block to execute at the next execution time provided by [getNextExecutionTime]
 * @return a cancelable [Task]
 * */
suspend fun HasContext.runAt(
    getNextLocalExecutionTime: () -> DateTimeTz,
    whenToUpdate: (doUpdate: suspend () -> Unit) -> Unit,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedIrregularTask(
        kHomeAssistant = getKHomeAssistant()!!,
        getNextUTCExecutionTime = { getNextLocalExecutionTime().utc },
        whenToUpdate = whenToUpdate,
        callback = callback
    )

    getKHomeAssistant()!!.schedule(task)

    return object : Task {
        override suspend fun cancel() {
            try {
                getKHomeAssistant()!!.cancel(task)
            } catch (e: Exception) {
            }
        }
    }

}

interface Task {
    suspend fun cancel()
}

sealed class RepeatedTask : Comparable<RepeatedTask> {
    abstract val kHomeAssistant: KHomeAssistant
    abstract val scheduledNextExecution: DateTime
    abstract val callback: suspend () -> Unit
    abstract suspend fun update()
    override fun compareTo(other: RepeatedTask) = scheduledNextExecution.compareTo(other.scheduledNextExecution)

    override fun toString() = scheduledNextExecution.toString()
}

class RepeatedRegularTask(
    override val kHomeAssistant: KHomeAssistant,
    alignWith: DateTime,
    val runEvery: TimeSpan,
    override val callback: suspend () -> Unit
) : RepeatedTask() {

    override var scheduledNextExecution = alignWith

    init {
        val now = DateTime.now()
        while (scheduledNextExecution < now)
            scheduledNextExecution += runEvery
    }

    override suspend fun update() {
        scheduledNextExecution += runEvery
        kHomeAssistant.reschedule(this)
    }
}

class RepeatedIrregularTask(
    override val kHomeAssistant: KHomeAssistant,
    val getNextUTCExecutionTime: () -> DateTime,
    whenToUpdate: (doUpdate: suspend () -> Unit) -> Unit,
    override val callback: suspend () -> Unit
) : RepeatedTask() {
    override var scheduledNextExecution: DateTime = getNextUTCExecutionTime()

    init {
        whenToUpdate {
            update()
        }
    }

    override suspend fun update() {
        scheduledNextExecution = getNextUTCExecutionTime()
        kHomeAssistant.reschedule(this)
    }
}