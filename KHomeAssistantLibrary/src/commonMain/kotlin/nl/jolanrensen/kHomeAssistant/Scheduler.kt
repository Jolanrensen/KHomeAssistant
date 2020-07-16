package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.*
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChanged

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

/** This is a [DateTimeTz] instance representing 00:00:00, Thursday, 1 January 1970, local time. */
val LOCAL_EPOCH: DateTimeTz = DateTime.EPOCH.localUnadjusted

// TODO
//fun KHomeAssistantContext.runEveryWeekAt(dayOfWeek: DayOfWeek, time: Time, callback: suspend () -> Unit): Task {
//    val offsetAtEpoch = LOCAL_EPOCH.offset.time
//    return runEvery(1.weeks, DateTime(DateTime.EPOCH.date, time).localUnadjusted - offsetAtEpoch, callback)
//}

/** Schedule something to execute each week at a certain day at a certain time. */
suspend fun KHomeAssistant.runEveryWeekAt(
    dayOfWeek: DayOfWeek,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit
) = runEveryWeek(
    dayOfWeek.index0Monday.days + hour.hours + minute.minutes + second.seconds + millisecond.milliseconds,
    callback
)

/** Schedule something to execute each week at a certain day at a certain time. */
suspend fun KHomeAssistant.runEveryWeekAt(
    dayOfWeek: DayOfWeek,
    time: Time,
    callback: suspend () -> Unit
) = runEveryWeek(dayOfWeek.index0Monday.days + time.encoded, callback)


/** Schedule something to execute at a certain (local) time each day. */
suspend fun KHomeAssistant.runEveryDayAt(
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit
) = runEveryDayAt(Time(hour, minute, second, millisecond), callback)

/** Schedule something to execute at a certain (local) time each day. */
suspend fun KHomeAssistant.runEveryDayAt(localTime: Time, callback: suspend () -> Unit): Task {
    val offsetAtEpoch = LOCAL_EPOCH.offset.time
    return runEvery(1.days, DateTime(DateTime.EPOCH.date, localTime).localUnadjusted - offsetAtEpoch, callback)
}

/** Schedule something to execute each week, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the week (Monday) will be picked. */
suspend fun KHomeAssistant.runEveryWeek(
    callback: suspend () -> Unit
) = runEvery(1.weeks, callback = callback)

/** Schedule something to execute each week, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the week (Monday) will be picked. */
suspend fun KHomeAssistant.runEveryWeek(
    alignWith: DateTimeTz = LOCAL_EPOCH + 4.days,
    callback: suspend () -> Unit
) = runEvery(1.weeks, alignWith, callback)

/** Schedule something to execute each week, optionally offset with a timeSpan (e.g. amount of days). If not offset, the beginning of the week (Monday) will be picked. */
suspend fun KHomeAssistant.runEveryWeek(
    offset: TimeSpan = 0.days + 0.hours + 0.minutes + 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit
) = runEvery(1.weeks, LOCAL_EPOCH + 4.days + offset, callback)


/** Schedule something to execute each day, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the day will be picked. */
suspend fun KHomeAssistant.runEveryDay(
    callback: suspend () -> Unit
) = runEvery(1.days, callback = callback)

/** Schedule something to execute each day, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the day will be picked. */
suspend fun KHomeAssistant.runEveryDay(
    alignWith: DateTimeTz = LOCAL_EPOCH,
    callback: suspend () -> Unit
) = runEvery(1.days, alignWith, callback)

/** Schedule something to execute each day, optionally offset with a timeSpan (e.g. amount of hours). If not offset, the beginning of the day will be picked. */
suspend fun KHomeAssistant.runEveryDay(
    offset: TimeSpan = 0.hours + 0.minutes + 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit
) = runEvery(1.days, LOCAL_EPOCH + offset, callback)

/** Schedule something to execute each hour, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the hour will be picked. */
suspend fun KHomeAssistant.runEveryHour(
    callback: suspend () -> Unit
) = runEvery(1.hours, callback = callback)

/** Schedule something to execute each hour, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the hour will be picked. */
suspend fun KHomeAssistant.runEveryHour(
    alignWith: DateTimeTz = LOCAL_EPOCH,
    callback: suspend () -> Unit
) = runEvery(1.hours, alignWith, callback)

/** Schedule something to execute each hour, optionally offset with a timeSpan (e.g. amount of minutes). If not offset, the beginning of the hour will be picked. */
suspend fun KHomeAssistant.runEveryHour(
    offset: TimeSpan = 0.minutes + 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit
) = runEvery(1.hours, LOCAL_EPOCH + offset, callback)

/** Schedule something to execute each minute, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute will be picked. */
suspend fun KHomeAssistant.runEveryMinute(
    callback: suspend () -> Unit
) = runEvery(1.minutes, callback = callback)

/** Schedule something to execute each minute, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute will be picked. */
suspend fun KHomeAssistant.runEveryMinute(
    alignWith: DateTimeTz = LOCAL_EPOCH,
    callback: suspend () -> Unit
) = runEvery(1.minutes, alignWith, callback)

/** Schedule something to execute each minute, optionally offset with a timespan (e.g. amount of seconds). If not offset, the beginning of the minute will be picked. */
suspend fun KHomeAssistant.runEveryMinute(
    offset: TimeSpan = 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit
) = runEvery(1.minutes, LOCAL_EPOCH + offset, callback)

/** Schedule something to execute each second, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the second will be picked. */
suspend fun KHomeAssistant.runEverySecond(
    callback: suspend () -> Unit
) = runEvery(1.seconds, callback = callback)

/** Schedule something to execute each second, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the second will be picked. */
suspend fun KHomeAssistant.runEverySecond(
    alignWith: DateTimeTz = LOCAL_EPOCH,
    callback: suspend () -> Unit
) = runEvery(1.seconds, alignWith, callback)

/** Schedule something to execute each second, optionally offset with a timeSpan (amount of milliseconds). If not offset, the beginning of the second will be picked. */
suspend fun KHomeAssistant.runEverySecond(
    offset: TimeSpan = 0.milliseconds,
    callback: suspend () -> Unit
) = runEvery(1.seconds, LOCAL_EPOCH + offset, callback)


/** Schedule something to repeatedly execute each given timespan, optionally aligned with a certain point in (local) time. If not aligned, the local epoch (00:00:00 jan 1 1970, local time) will be picked. */
suspend fun KHomeAssistant.runEvery(
    timeSpan: TimeSpan,
    alignWith: DateTimeTz = LOCAL_EPOCH,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedRegularTask(
        kHomeAssistant = instance,
        alignWith = alignWith.utc,
        runEvery = timeSpan,
        callback = callback
    )

    instance.schedule(task)

    return object : Task {
        override suspend fun cancel() {
            try { // TODO not sure if try catch necessary
                instance.cancel(task)
            } catch (e: Exception) {
            }
        }
    }
}

/** Schedule something to execute each day at sunrise. */
suspend fun KHomeAssistant.runEveryDayAtSunrise(offset: TimeSpan = TimeSpan.ZERO, callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.nextRising + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextRising) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at sunset. */
suspend fun KHomeAssistant.runEveryDayAtSunset(offset: TimeSpan = TimeSpan.ZERO, callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.nextSetting + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextSetting) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at dawn. */
suspend fun KHomeAssistant.runEveryDayAtDawn(offset: TimeSpan = TimeSpan.ZERO, callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.nextDawn + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextDawn) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at dusk. */
suspend fun KHomeAssistant.runEveryDayAtDusk(offset: TimeSpan = TimeSpan.ZERO, callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.nextDusk + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextDusk) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at noon. */
suspend fun KHomeAssistant.runEveryDayAtNoon(offset: TimeSpan = TimeSpan.ZERO, callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.nextNoon + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextNoon) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at midnight. */
suspend fun KHomeAssistant.runEveryDayAtMidnight(offset: TimeSpan = TimeSpan.ZERO, callback: suspend () -> Unit) =
    runAt(
        getNextLocalExecutionTime = { sun.nextMidnight + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextMidnight) { update() } } },
        callback = callback
    )

/** Schedule something to execute after [timeSpan] amount of time from now. */
suspend fun KHomeAssistant.runIn(timeSpan: TimeSpan, callback: suspend () -> Unit): Task =
    runAt(DateTimeTz.nowLocal() + timeSpan, callback)

/** Schedule something to execute at a given point in (local) time. The task will automatically be canceled after execution. */
suspend fun KHomeAssistant.runAt(
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

/** Schedule something to run at a point in (local) time that can be obtained using the given getNextLocalExecutionTime callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI.
 * @see runEveryDayAt for use with a [Time], so no date, just the time.
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
suspend fun KHomeAssistant.runAt(
    getNextLocalExecutionTime: () -> DateTimeTz,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    callback: suspend () -> Unit
): Task {
    val task = RepeatedIrregularTask(
        kHomeAssistant = instance,
        getNextUTCExecutionTime = { getNextLocalExecutionTime().utc },
        whenToUpdate = whenToUpdate,
        callback = callback
    )

    instance.schedule(task)

    return object : Task {
        override suspend fun cancel() {
            try {
                instance.cancel(task)
            } catch (e: Exception) {
            }
        }
    }
}

/** Schedule something to run at a point in (local) time that can be obtained using the given getNextLocalExecutionTime callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI.
 * @see runAt for use with a [DateTimeTz], so a date and time.
 *
 * ```
 * val task = runEveryDayAt(
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
suspend fun KHomeAssistant.runEveryDayAt(
    getNextLocalExecutionTime: () -> Time,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    callback: suspend () -> Unit
): Task = runAt(
    getNextLocalExecutionTime = {
        var dateTime =
            DateTime(
                date = DateTimeTz.nowLocal().local.date,
                time = getNextLocalExecutionTime()
            ).localUnadjusted
        if (dateTime < DateTimeTz.nowLocal()) dateTime += 1.days
        dateTime
    },
    whenToUpdate = { update ->
        // making sure the dateTime updates to a point in time today at the start of each day
        runEveryDay(update)
        whenToUpdate(update)
    },
    callback = callback
)


interface Task {
    suspend fun cancel()
}

sealed class RepeatedTask : Comparable<RepeatedTask> {
    abstract val kHomeAssistant: KHomeAssistantInstance
    abstract val scheduledNextExecution: DateTime
    abstract val callback: suspend () -> Unit
    abstract suspend fun update()
    override fun compareTo(other: RepeatedTask) = scheduledNextExecution.compareTo(other.scheduledNextExecution)

    override fun toString() = scheduledNextExecution.toString()
}

class RepeatedRegularTask(
    override val kHomeAssistant: KHomeAssistantInstance,
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
    override val kHomeAssistant: KHomeAssistantInstance,
    val getNextUTCExecutionTime: () -> DateTime,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    override val callback: suspend () -> Unit
) : RepeatedTask() {
    override var scheduledNextExecution: DateTime = getNextUTCExecutionTime()

    init {
        runBlocking {
            whenToUpdate {
                update()
            }
        }
    }

    override suspend fun update() {
        scheduledNextExecution = getNextUTCExecutionTime()
        kHomeAssistant.reschedule(this)
    }
}