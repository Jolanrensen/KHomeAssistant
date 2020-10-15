package nl.jolanrensen.kHomeAssistant

import com.soywiz.klock.Time
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import nl.jolanrensen.kHomeAssistant.TwelveHourPeriod.AM
import nl.jolanrensen.kHomeAssistant.TwelveHourPeriod.PM
import kotlin.time.*

@OptIn(ExperimentalTime::class)
public class TimeOfDay(private val duration: Duration) : Comparable<TimeOfDay> {

    public companion object {
        public val MIDNIGHT: TimeOfDay = TimeOfDay(hours = 0)
        public val NOON: TimeOfDay = TimeOfDay(hours = 12)

        // due to JVM declaration clash
        public operator fun invoke(ktorTime: Time): TimeOfDay = TimeOfDay(ktorTime.encoded.milliseconds.milliseconds)
    }

    // TODO add AM/PM

    public val durationSinceStartOfDay: Duration
        get() = duration

    public val hoursComponent: Int
        get() = durationSinceStartOfDay.toComponents { _, hours, _, _, _ -> hours }

    public val minutesComponent: Int
        get() = durationSinceStartOfDay.toComponents { _, _, minutes, _, _ -> minutes }

    public val secondsComponent: Int
        get() = durationSinceStartOfDay.toComponents { _, _, _, seconds, _ -> seconds }

    public val nanosecondsComponent: Int
        get() = durationSinceStartOfDay.toComponents { _, _, _, _, nanoseconds -> nanoseconds }

    init {
        if (duration >= 24.hours) throw IllegalArgumentException("A time of day must be smaller than 24 hours.")
    }

    override fun compareTo(other: TimeOfDay): Int = duration.compareTo(duration)

//    public constructor(ktorTime: Time) : this(ktorTime.encoded.milliseconds.milliseconds)

    public constructor(timeOfDay: TimeOfDay) : this(timeOfDay.duration)

    public constructor(
        hours: Int,
        minutes: Int = 0,
        seconds: Int = 0,
        milliseconds: Int = 0,
        microseconds: Int = 0,
        nanoseconds: Int = 0,
    ) : this(
        hours.hours +
                minutes.minutes +
                seconds.seconds +
                milliseconds.milliseconds +
                microseconds.microseconds +
                nanoseconds.nanoseconds
    )

    /**
     * AM / PM constructor
     */
    public constructor(
        hours: Int,
        minutes: Int = 0,
        seconds: Int = 0,
        milliseconds: Int = 0,
        microseconds: Int = 0,
        nanoseconds: Int = 0,
        twelveHourPeriod: TwelveHourPeriod,
    ) : this({
        var summation = hours.hours +
                minutes.minutes +
                seconds.seconds +
                milliseconds.milliseconds +
                microseconds.microseconds +
                nanoseconds.nanoseconds

        if (summation >= 24.hours) throw IllegalArgumentException("A time of day must be smaller than 24 hours.")

        if (summation < 1.hours) throw IllegalArgumentException("0:XX AM does not exist, use 12:XX AM.")
        if (summation >= 13.hours) throw IllegalArgumentException("When using AM/PM, 13:00 and after do not exist.")

        if (twelveHourPeriod == PM && summation >= 1.hours && summation < 12.hours)
            summation += 12.hours
        if (twelveHourPeriod == AM && summation >= 12.hours)
            summation -= 12.hours

        summation
    }())
}


@OptIn(ExperimentalTime::class)
public operator fun TimeOfDay.plus(duration: Duration): TimeOfDay = TimeOfDay(duration + durationSinceStartOfDay)

public val LocalDateTime.time: TimeOfDay
    get() = TimeOfDay(
        hours = hour,
        minutes = minute,
        seconds = second,
        nanoseconds = nanosecond,
    )

public fun LocalDateTime(date: LocalDate, time: TimeOfDay): LocalDateTime =
    LocalDateTime(
        year = date.year,
        month = date.month,
        dayOfMonth = date.dayOfMonth,
        hour = time.hoursComponent,
        minute = time.minutesComponent,
        second = time.secondsComponent,
        nanosecond = time.nanosecondsComponent,
    )

public interface TimeOfDayTwelveHours {
    public val timeOfDay: TimeOfDay
    public val amOrPm: TwelveHourPeriod
}

public enum class TwelveHourPeriod {
    AM, PM
}