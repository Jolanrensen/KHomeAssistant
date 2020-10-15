package nl.jolanrensen.kHomeAssistant.helper

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

public interface DaysHoursMinutesSecondsNanoseconds {
    val days: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val nanoseconds: Int

    operator fun component1(): Int = days
    operator fun component2(): Int = hours
    operator fun component3(): Int = minutes
    operator fun component4(): Int = seconds
    operator fun component5(): Int = nanoseconds
}

@OptIn(ExperimentalTime::class)
public fun Duration.toDaysHoursMinutesSecondsNanoseconds(): DaysHoursMinutesSecondsNanoseconds {
    val days: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val nanoseconds: Int

    toComponents { d, h, m, s, n ->
        days = d
        hours = h
        minutes = m
        seconds = s
        nanoseconds = n
    }

    return object : DaysHoursMinutesSecondsNanoseconds {
        override val days = days
        override val hours = hours
        override val minutes = minutes
        override val seconds = seconds
        override val nanoseconds = nanoseconds
    }
}

public interface HoursMinutesSecondsNanoseconds {
    val hours: Int
    val minutes: Int
    val seconds: Int
    val nanoseconds: Int

    operator fun component1(): Int = hours
    operator fun component2(): Int = minutes
    operator fun component3(): Int = seconds
    operator fun component4(): Int = nanoseconds
}

@OptIn(ExperimentalTime::class)
public fun Duration.toHoursMinutesSecondsNanoseconds(): HoursMinutesSecondsNanoseconds {
    val hours: Int
    val minutes: Int
    val seconds: Int
    val nanoseconds: Int

    toComponents { h, m, s, n ->
        hours = h
        minutes = m
        seconds = s
        nanoseconds = n
    }

    return object : HoursMinutesSecondsNanoseconds {
        override val hours = hours
        override val minutes = minutes
        override val seconds = seconds
        override val nanoseconds = nanoseconds
    }
}

public interface MinutesSecondsNanoseconds {
    val minutes: Int
    val seconds: Int
    val nanoseconds: Int

    operator fun component1(): Int = minutes
    operator fun component2(): Int = seconds
    operator fun component3(): Int = nanoseconds
}

@OptIn(ExperimentalTime::class)
public fun Duration.toMinutesSecondsNanoseconds(): MinutesSecondsNanoseconds {
    val minutes: Int
    val seconds: Int
    val nanoseconds: Int

    toComponents { m, s, n ->
        minutes = m
        seconds = s
        nanoseconds = n
    }

    return object : MinutesSecondsNanoseconds {
        override val minutes = minutes
        override val seconds = seconds
        override val nanoseconds = nanoseconds
    }
}

public interface SecondsNanoseconds {
    val seconds: Long
    val nanoseconds: Int

    operator fun component1(): Long = seconds
    operator fun component2(): Int = nanoseconds
}

@OptIn(ExperimentalTime::class)
public fun Duration.toSecondsNanoseconds(): SecondsNanoseconds {
    val seconds: Long
    val nanoseconds: Int

    toComponents { s, n ->
        seconds = s
        nanoseconds = n
    }

    return object : SecondsNanoseconds {
        override val seconds = seconds
        override val nanoseconds = nanoseconds
    }
}