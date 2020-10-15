@file:OptIn(ExperimentalTime::class)


package nl.jolanrensen.kHomeAssistant

import kotlinx.datetime.*
import kotlinx.datetime.Clock
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistantInstance
import nl.jolanrensen.kHomeAssistant.domains.sun
import nl.jolanrensen.kHomeAssistant.entities.invoke
import nl.jolanrensen.kHomeAssistant.entities.onAttributeChanged
import nl.jolanrensen.kHomeAssistant.helper.toHoursMinutesSecondsNanoseconds
import kotlin.jvm.Volatile
import kotlin.time.*

/**
 * The scheduler always assumes local time instead of UTC time to give you a piece of mind.
 * You can create a "local time" LocalDateTime by defining the local date/time you want in a normal Instantobject and
 * calling 'yourDateTime.localUnadjusted'. This will for instance make the 9 o'clock you entered in the Instantobject
 * remain 9 o'clock in the LocalDateTime object.
 *
 * If you do want to work with (UTC) Instantand (local) LocalDateTime, you can convert between the two like this:
 *
 *     'val myLocalTime: LocalDateTime = myUtcDateTime.local'
 * and
 *     'val myUtcTime: Instant= myLocalLocalDateTime.utc'
 */

/** This is a [LocalDateTime] instance representing 00:00:00, Thursday, 1 January 1970, local time. */
private val LOCAL_EPOCH: LocalDateTime = LocalDateTime(year = 1970, month = Month.JANUARY, dayOfMonth = 1, hour = 0, minute = 0)
private val LOCAL_EPOCH_INSTANT: Instant = LOCAL_EPOCH.toInstant(TimeZone.currentSystemDefault())
private val EPOCH: Instant = Instant.fromEpochSeconds(0)

//    DateTime.EPOCH.localUnadjusted

// TODO
//fun KHomeAssistantContext.runEveryWeekAt(dayOfWeek: DayOfWeek, time: Time, callback: suspend () -> Unit): Task {
//    val offsetAtEpoch = LOCAL_EPOCH.offset.time
//    return runEvery(1.weeks, DateTime(DateTime.EPOCH.date, time).localUnadjusted - offsetAtEpoch, callback)
//}

/** Schedule something to execute each week at a certain day at a certain time. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryWeekAt(
    dayOfWeek: DayOfWeek,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit,
): Task = runEveryWeek(
    dayOfWeek.isoDayNumber.days + hour.hours + minute.minutes + second.seconds + millisecond.milliseconds,
    callback
)

/** Schedule something to execute each week at a certain day at a certain time. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryWeekAt(
    dayOfWeek: DayOfWeek,
    time: TimeOfDay,
    callback: suspend () -> Unit,
): Task = runEveryWeek(dayOfWeek.isoDayNumber.days + time.durationSinceStartOfDay, callback)


/** Schedule something to execute at a certain (local) time each day. */
public suspend fun KHomeAssistant.runEveryDayAt(
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
    callback: suspend () -> Unit,
): Task = runEveryDayAt(TimeOfDay(hour, minute, second, millisecond), callback)

/** Schedule something to execute at a certain (local) time each day. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAt(localTime: TimeOfDay, callback: suspend () -> Unit): Task =
    runEveryDayAt(
        getNextLocalExecutionTime = { localTime },
        whenToUpdate = {},
        callback = callback
    )

/** Schedule something to execute each week, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the week (Monday) will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryWeek(
    callback: suspend () -> Unit,
): Task = runEvery(7.days, callback = callback)

/** Schedule something to execute each week, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the week (Monday) will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryWeek(
    alignWith: LocalDateTime = (LOCAL_EPOCH_INSTANT + 4.days).toLocalDateTime(TimeZone.currentSystemDefault()),
    callback: suspend () -> Unit,
): Task = runEvery(7.days, alignWith, callback)

/** Schedule something to execute each week, optionally offset with a duration (e.g. amount of days). If not offset, the beginning of the week (Monday) will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryWeek(
    offset: Duration = 0.days + 0.hours + 0.minutes + 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit,
): Task = runEvery(7.days, LOCAL_EPOCH_INSTANT + 4.days + offset, callback)


/** Schedule something to execute each day, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the day will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDay(
    callback: suspend () -> Unit,
): Task = runEvery(1.days, callback = callback)

/** Schedule something to execute each day, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the day will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDay(
    alignWith: LocalDateTime = LOCAL_EPOCH,
    callback: suspend () -> Unit,
): Task = runEvery(1.days, alignWith, callback)

/** Schedule something to execute each day, optionally offset with a duration (e.g. amount of hours). If not offset, the beginning of the day will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDay(
    offset: Duration = 0.hours + 0.minutes + 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit,
): Task = runEvery(1.days, LOCAL_EPOCH_INSTANT + offset, callback)

/** Schedule something to execute each hour, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the hour will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryHour(
    callback: suspend () -> Unit,
): Task = runEvery(1.hours, callback = callback)

/** Schedule something to execute each hour, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the hour will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryHour(
    alignWith: LocalDateTime = LOCAL_EPOCH,
    callback: suspend () -> Unit,
): Task = runEvery(1.hours, alignWith, callback)

/** Schedule something to execute each hour, optionally offset with a duration (e.g. amount of minutes). If not offset, the beginning of the hour will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryHour(
    offset: Duration = 0.minutes + 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit,
): Task = runEvery(1.hours, LOCAL_EPOCH_INSTANT + offset, callback)

/** Schedule something to execute each minute, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryMinute(
    callback: suspend () -> Unit,
): Task = runEvery(1.minutes, callback = callback)

/** Schedule something to execute each minute, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the minute will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryMinute(
    alignWith: LocalDateTime = LOCAL_EPOCH,
    callback: suspend () -> Unit,
): Task = runEvery(1.minutes, alignWith, callback)

/** Schedule something to execute each minute, optionally offset with a timespan (e.g. amount of seconds). If not offset, the beginning of the minute will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryMinute(
    offset: Duration = 0.seconds + 0.milliseconds,
    callback: suspend () -> Unit,
): Task = runEvery(1.minutes, LOCAL_EPOCH_INSTANT + offset, callback)

/** Schedule something to execute each second, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the second will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEverySecond(
    callback: suspend () -> Unit,
): Task = runEvery(1.seconds, callback = callback)

/** Schedule something to execute each second, optionally aligned with a certain point in (local) time. If not aligned, the beginning of the second will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEverySecond(
    alignWith: LocalDateTime = LOCAL_EPOCH,
    callback: suspend () -> Unit,
): Task = runEvery(1.seconds, alignWith, callback)

/** Schedule something to execute each second, optionally offset with a duration (amount of milliseconds). If not offset, the beginning of the second will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEverySecond(
    offset: Duration = 0.milliseconds,
    callback: suspend () -> Unit,
): Task = runEvery(1.seconds, LOCAL_EPOCH_INSTANT + offset, callback)


/** Schedule something to repeatedly execute each given timespan, optionally aligned with a certain point in (local) time. If not aligned, the local epoch (00:00:00 jan 1 1970, local time) will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEvery(
    duration: Duration,
    alignWith: LocalDateTime = LOCAL_EPOCH,
    callback: suspend () -> Unit,
): Task = runEvery(
    duration = duration,
    alignWith = alignWith.toInstant(TimeZone.currentSystemDefault()),
    callback = callback
)

/** Schedule something to repeatedly execute each given timespan, optionally aligned with a certain point in time. If not aligned, the local epoch (00:00:00 jan 1 1970, local time) will be picked. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEvery(
    duration: Duration,
    alignWith: Instant,
    callback: suspend () -> Unit,
): Task {
    val task = RepeatedRegularTask(
        kHomeAssistant = instance,
        alignWith = alignWith,
        runEvery = duration,
        callback = callback
    )

    instance.schedule(task)

    return object : Task {
        override public suspend fun cancel() {
            try { // TODO not sure if try catch necessary
                instance.cancel(task)
            } catch (e: Exception) {
            }
        }
    }
}

/** Schedule something to execute each day at sunrise. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAtSunrise(offset: Duration = Duration.ZERO, callback: suspend () -> Unit): Task =
    runAtInstant(
        getNextExecutionInstant = { sun.nextRising.toInstant(TimeZone.currentSystemDefault()) + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextRising) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at sunset. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAtSunset(offset: Duration = Duration.ZERO, callback: suspend () -> Unit): Task =
    runAtInstant(
        getNextExecutionInstant = { sun.nextSetting.toInstant(TimeZone.currentSystemDefault()) + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextSetting) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at dawn. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAtDawn(offset: Duration = Duration.ZERO, callback: suspend () -> Unit): Task =
    runAtInstant(
        getNextExecutionInstant = { sun.nextDawn.toInstant(TimeZone.currentSystemDefault()) + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextDawn) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at dusk. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAtDusk(offset: Duration = Duration.ZERO, callback: suspend () -> Unit): Task =
    runAtInstant(
        getNextExecutionInstant = { sun.nextDusk.toInstant(TimeZone.currentSystemDefault()) + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextDusk) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at noon. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAtNoon(offset: Duration = Duration.ZERO, callback: suspend () -> Unit): Task =
    runAtInstant(
        getNextExecutionInstant = { sun.nextNoon.toInstant(TimeZone.currentSystemDefault()) + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextNoon) { update() } } },
        callback = callback
    )

/** Schedule something to execute each day at midnight. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAtMidnight(offset: Duration = Duration.ZERO, callback: suspend () -> Unit): Task =
    runAtInstant(
        getNextExecutionInstant = { sun.nextMidnight.toInstant(TimeZone.currentSystemDefault()) + offset },
        whenToUpdate = { update -> sun { onAttributeChanged(::nextMidnight) { update() } } },
        callback = callback
    )

/** Schedule something to execute after [duration] amount of time from now. */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runIn(duration: Duration, callback: suspend () -> Unit): Task =
    runAtInstant(Clock.System.now() + duration, callback)

/** Schedule something to execute at a given point in (local) time. The task will automatically be canceled after execution. */
public suspend fun KHomeAssistant.runAt(
    localDateTime: LocalDateTime,
    callback: suspend () -> Unit,
): Task = runAtInstant(
    instant = localDateTime.toInstant(TimeZone.currentSystemDefault()),
    callback = callback
)

/** Schedule something to execute at a given point in time. The task will automatically be canceled after execution. */
public suspend fun KHomeAssistant.runAtInstant(
    instant: Instant,
    callback: suspend () -> Unit,
): Task {
    var cancel: (suspend () -> Unit)? = null
    val task = runAtInstant(
        getNextExecutionInstant = { instant },
        whenToUpdate = {}
    ) {
        callback()
        cancel!!.invoke()
    }
    cancel = task::cancel

    return task
}

/** Schedule something to run at a point in (local) time that can be obtained using the given [getNextLocalExecutionTime] callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI.
 * @see runEveryDayAt for use with a [Time], so no date, just the time.
 * ```
 * val task = runAt(
 *    getNextExecutionTime = { someLocalDateTime },
 *    whenToUpdate = { doUpdate -> someEntity.onStateChanged { doUpdate() } }
 * ) {
 *     // do something
 * }
 * ``
 * @receiver the [KHomeAssistantContext] inheriting context (like [Automation]) from which to call it
 * @param getNextExecutionTime a function to get the next execution time in [LocalDateTime]
 * @param whenToUpdate a function providing a `doUpdate` function which should be executed when the value returned at [getNextExecutionTime] has changed
 * @param callback the code block to execute at the next execution time provided by [getNextExecutionTime]
 * @return a cancelable [Task]
 * */
public suspend fun KHomeAssistant.runAt(
    getNextLocalExecutionTime: () -> LocalDateTime,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    callback: suspend () -> Unit,
): Task = runAtInstant(
    getNextExecutionInstant = { getNextLocalExecutionTime().toInstant(TimeZone.currentSystemDefault()) },
    whenToUpdate = whenToUpdate,
    callback = callback
)

/** Schedule something to run at a point in  time that can be obtained using the given [getNextExecutionInstant] callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI.
 * @see runEveryDayAt for use with a [Time], so no date, just the time.
 * ```
 * val task = runAt(
 *    getNextExecutionInstant = { someInstantInTime },
 *    whenToUpdate = { doUpdate -> someEntity.onStateChanged { doUpdate() } }
 * ) {
 *     // do something
 * }
 * ``
 * @receiver the [KHomeAssistantContext] inheriting context (like [Automation]) from which to call it
 * @param getNextExecutionTime a function to get the next execution time in [LocalDateTime]
 * @param whenToUpdate a function providing a `doUpdate` function which should be executed when the value returned at [getNextExecutionTime] has changed
 * @param callback the code block to execute at the next execution time provided by [getNextExecutionTime]
 * @return a cancelable [Task]
 * */
public suspend fun KHomeAssistant.runAtInstant(
    getNextExecutionInstant: () -> Instant,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    callback: suspend () -> Unit,
): Task {
    val task = RepeatedIrregularTask(
        kHomeAssistant = instance,
        getNextExecutionTimeInstant = { getNextExecutionInstant() },
        whenToUpdate = whenToUpdate,
        callback = callback
    )

    instance.schedule(task)

    return object : Task {
        override public suspend fun cancel() {
            try {
                instance.cancel(task)
            } catch (e: Exception) {
            }
        }
    }
}

/** Schedule something to run at a point in (local) time that can be obtained using the given getNextLocalExecutionTime callback.
 * This is ideal for sunsets or -rises, that shift every day, or, for instance, for scheduling something to execute based on an input_datetime from the Home Assistant UI.
 * @see runAtInstant for use with a [LocalDateTime], so a date and time.
 *
 * ```
 * val task = runEveryDayAt(
 *    getNextExecutionTime = { someLocalDateTime },
 *    whenToUpdate = { doUpdate -> someEntity.onStateChanged { doUpdate() } }
 * ) {
 *     // do something
 * }
 * ``
 * @receiver the [KHomeAssistantContext] inheriting context (like [Automation]) from which to call it
 * @param getNextExecutionTime a function to get the next execution time in [LocalDateTime]
 * @param whenToUpdate a function providing a `doUpdate` function which should be executed when the value returned at [getNextExecutionTime] has changed
 * @param callback the code block to execute at the next execution time provided by [getNextExecutionTime]
 * @return a cancelable [Task]
 * */
@OptIn(ExperimentalTime::class)
public suspend fun KHomeAssistant.runEveryDayAt(
    getNextLocalExecutionTime: () -> TimeOfDay,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    callback: suspend () -> Unit,
): Task = runAtInstant(
    getNextExecutionInstant = {
        val currentLocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val (hours, minutes, seconds, nanoseconds) = getNextLocalExecutionTime()
            .durationSinceStartOfDay
            .toHoursMinutesSecondsNanoseconds()

        var instant = LocalDateTime(
            year = currentLocalDate.year,
            month = currentLocalDate.month,
            dayOfMonth = currentLocalDate.dayOfMonth,
            hour = hours,
            minute = minutes,
            second = seconds,
            nanosecond = nanoseconds,
        ).toInstant(TimeZone.currentSystemDefault())

        if (instant < Clock.System.now()) instant += 1.days

        instant
    },
    whenToUpdate = { update ->
        // making sure the dateTime updates to a point in time today at the start of each day
        runEveryDay(update)
        whenToUpdate(update)
    },
    callback = callback
)


/* TODO fun */ public interface Task {
    public suspend fun cancel()
}

public sealed class RepeatedTask : Comparable<RepeatedTask> {
    public abstract val kHomeAssistant: KHomeAssistantInstance
    public abstract val scheduledNextExecution: Instant
    public abstract val callback: suspend () -> Unit
    public abstract suspend fun update()
    override fun compareTo(other: RepeatedTask): Int = scheduledNextExecution.compareTo(other.scheduledNextExecution)

    public abstract var lastExecutionScheduledExecutionTime: Instant

    override fun toString(): String = scheduledNextExecution.toString()
}

@OptIn(ExperimentalTime::class)
public class RepeatedRegularTask(
    override val kHomeAssistant: KHomeAssistantInstance,
    alignWith: Instant,
    public val runEvery: Duration,
    override val callback: suspend () -> Unit,
) : RepeatedTask() {

    @Volatile
    override var lastExecutionScheduledExecutionTime: Instant = Instant.DISTANT_PAST

    @Volatile
    override var scheduledNextExecution: Instant = alignWith

    init {
        val now = Clock.System.now()
        while (scheduledNextExecution < now)
            scheduledNextExecution += runEvery
    }

    public override suspend fun update() {
        scheduledNextExecution += runEvery
        kHomeAssistant.reschedule(this)
    }
}

public class RepeatedIrregularTask(
    override val kHomeAssistant: KHomeAssistantInstance,
    public val getNextExecutionTimeInstant: () -> Instant,
    whenToUpdate: suspend (update: suspend () -> Unit) -> Unit,
    override val callback: suspend () -> Unit,
) : RepeatedTask() {

    @Volatile
    override var lastExecutionScheduledExecutionTime: Instant = Instant.DISTANT_PAST

    @Volatile
    override var scheduledNextExecution: Instant = getNextExecutionTimeInstant()

    init {
        runBlocking {
            whenToUpdate {
                update()
            }
        }
    }

    public override suspend fun update() {
        scheduledNextExecution = getNextExecutionTimeInstant()
        kHomeAssistant.reschedule(this)
    }
}