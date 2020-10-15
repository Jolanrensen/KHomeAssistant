package nl.jolanrensen.kHomeAssistant.domains.input

import com.soywiz.klock.TimeFormat.Companion.FORMAT_TIME
import com.soywiz.klock.parseTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

/**
 * Input Datetime
 * https://www.home-assistant.io/integrations/input_datetime
 * */
public class InputDatetime(override val kHassInstance: KHomeAssistant) : Domain<InputDatetime.Entity> {
    override val domainName: String = "input_datetime"

    /** Making sure InputDatetime acts as a singleton. */
    override fun equals(other: Any?): Boolean = other is InputDatetime
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_datetime configuration. */
    public suspend fun reload(): ResultMessage = callService("reload")

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    public class State(public val value: String) {
        /** Only use if `has_time == true && has_date == false`. */
        public val asTime: TimeOfDay
            get() = TimeOfDay(FORMAT_TIME.parseTime(value))

        /** Only use if `has_time == false && has_date == true`. */
        public val asDate: LocalDate
            get() = LocalDate.parse(value)

        /** Only use if `has_time == true && has_date == true`. */
        public val asDateTime: LocalDateTime
            get() = value.split(" ").let {
                val date = LocalDate.parse(it.first())
                val time = TimeOfDay(FORMAT_TIME.parseTime(it.last()))

                LocalDateTime(
                    date = date,
                    time = time,
                )
            }
    }

    public interface HassAttributes : BaseHassAttributes {
        // read only

        /** true if this entity has a time. */
        val has_time: Boolean

        /** true if this entity has a date. */
        val has_date: Boolean

        /** A UNIX timestamp representing the (UTC) date and time (in seconds) held in the input. */
        val timestamp: Long

        /** True of this input datetime is editable. */
        val editable: Boolean


        // read / write

        /** The year of the date. */
        var year: Int

        /** The month of the date. @see [month_]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("month_"))
        var month: Int

        /** The day (of the month) of the date, if [has_date]. */
        var day: Int

        /** The hour of the time, if [has_time]. */
        var hour: Int

        /** The minute of the time, if [has_time]. */
        var minute: Int

        /** The second of the time, if [has_time]. */
        var second: Int

        // Helpers

        /** true if this entity has a date and a time */
        val hasDateAndTime: Boolean
            get() = has_date && has_time


        /** The month of the date, if [has_date]. Use `month_.index1` to get the month as [Int] (with January == 1). */
        var month_: Month
            get() = Month(month)
            set(value) {
                month = value.number
            }

        /** The date (in local time) as represented in Home Assistant, if [has_date]. */
        var date: LocalDate
            get() = LocalDate(year, month, day)
            set(value) {
                year = value.year
                month = value.monthNumber
                day = value.dayOfMonth
            }

        /** The time (in local time) as represented in Home Assistant, if [has_time]. */
        public var time: TimeOfDay
            get() = TimeOfDay(hours = hour, minutes = minute, seconds = second)
            set(value) {
                hour = value.hoursComponent
                minute = value.minutesComponent
                second = value.secondsComponent
            }

        /** The date and time (in local time) as represented in Home Assistant, if [has_date] and [has_time].
         * Convert to [DateTime] using UTC time with `dateTime!!.utc`.
         * Convert to [DateTime] using local time with `dateTime!!.local`.
         * ([DateTime] is usually only for calculating with UTC time, but it has more functionality than [DateTimeTz])
         * */
        public var dateTime: LocalDateTime
            get() = LocalDateTime(date, time)
            set(value) {
                date = value.date
                time = value.time
            }
    }

    @OptIn(ExperimentalStdlibApi::class)
    public class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String,
    ) : BaseEntity<State, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputDatetime(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        override fun stringToState(stateValue: String): State = State(stateValue)
        override fun stateToString(state: State): String = state.value

        /** Some attributes can be set using the set_datetime command. For those, we define a setter-companion to getValue. */
        override suspend fun <V : Any?> setValue(
            propertyName: String,
            value: V,
        ) {
            when (propertyName) {
                ::year.name -> try {
                    setDate(localDate = LocalDate(year = value as Int, monthNumber = month, dayOfMonth = day))
                } catch (e: Exception) {
                    throw Exception("Can't set year, $name has no date.", e)
                }
                ::month.name -> try {
                    setDate(localDate = LocalDate(year = year, monthNumber = value as Int, dayOfMonth = day))
                } catch (e: Exception) {
                    throw Exception("Can't set month, $name has no date.", e)
                }
                ::day.name -> try {
                    setDate(localDate = LocalDate(year = year, monthNumber = month, dayOfMonth = value as Int))
                } catch (e: Exception) {
                    throw Exception("Can't set day, $name has no date.", e)
                }
                ::hour.name -> try {
                    setTime(localTime = TimeOfDay(hours = value as Int, minutes = minute, seconds = second))
                } catch (e: Exception) {
                    throw Exception("Can't set hour, $name has no time.", e)
                }
                ::minute.name -> try {
                    setTime(localTime = TimeOfDay(hours = hour, minutes = value as Int, seconds = second))
                } catch (e: Exception) {
                    throw Exception("Can't set minute, $name has no time.", e)
                }
                ::second.name -> try {
                    setTime(localTime = TimeOfDay(hours = hour, minutes = minute, seconds = value as Int))
                } catch (e: Exception) {
                    throw Exception("Can't set second, $name has no time.", e)
                }
            }
        }

        override val has_time: Boolean by attrsDelegate(false)
        override val has_date: Boolean by attrsDelegate(false)
        override val timestamp: Long by attrsDelegate()
        override val editable: Boolean by attrsDelegate()

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("year_"))
        override var year: Int by attrsDelegate()

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("month_"))
        override var month: Int by attrsDelegate()
        override var day: Int by attrsDelegate()
        override var hour: Int by attrsDelegate()
        override var minute: Int by attrsDelegate()
        override var second: Int by attrsDelegate()
        override var date: LocalDate
            get() = super.date
            set(value) {
                runBlocking {
                    setDate(localDate = value)
                }
            }
        override var time: TimeOfDay
            get() = super.time
            set(value) {
                runBlocking {
                    setTime(localTime = value)
                }
            }
        override var dateTime: LocalDateTime
            get() = super.dateTime
            set(value) {
                runBlocking {
                    setDateTime(localDateTime = value)
                }
            }

        /**
         * Specify something to run at the specified date time value of the entity.
         * It gets updated when the state of the entity changes.
         */
        public suspend fun runAtDateTime(callback: suspend Entity.() -> Unit): Entity {
            if (!has_date) throw IllegalArgumentException("entity $name does not have a date.")
            if (!has_time) throw IllegalArgumentException("entity $name does not have a time.")
            kHassInstance.runAt(
                getNextLocalExecutionTime = { dateTime },
                whenToUpdate = { update -> onAttributeChanged(::dateTime) { update() } }
            ) {
                callback()
            }
            return this
        }

        /**
         * Specify something to run at the specified date value of the entity and the given time.
         * It gets updated when the state of the entity changes.
         */
        public suspend fun runAtDate(localTime: TimeOfDay = TimeOfDay(0), callback: suspend Entity.() -> Unit): Entity {
            if (!has_date) throw IllegalArgumentException("entity $name does not have a date.")
            kHassInstance.runAt(
                getNextLocalExecutionTime = { LocalDateTime(date, localTime) },
                whenToUpdate = { update -> onAttributeChanged(::date) { update() } }
            ) {
                callback()
            }
            return this
        }

        /**
         * Specify something to run at the specified time value of this entity every day.
         * It gets updated when the state of the entity changes.
         */
        public suspend fun runEveryDayAtTime(callback: suspend Entity.() -> Unit): Entity {
            if (!has_time) throw IllegalArgumentException("entity $name does not have a time.")
            kHassInstance.runEveryDayAt(
                { time },
                { update -> onAttributeChanged(::time) { update() } }
            ) {
                callback()
            }
            return this
        }

        /** Set the state value. To make a [DateTimeTz] (like 9 am local time), use `DateTime(someDate, Time(hour=9)).localUnadjusted`. */
        public suspend fun setDateTime(localDateTime: LocalDateTime, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = buildJsonObject {
                    localDateTime.let {
                        if (has_date) {
                            put("date", ("${it.year}-${it.monthNumber}-${it.dayOfMonth}"))
                        }

                        if (has_time)
                            put("time", "${it.hour}:${it.minute}:${it.second}")
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::dateTime, localDateTime)
            return result
        }


        /** Set the state value, but just the date part. The date must be in local time for Home Assistant. */
        public suspend fun setDate(localDate: LocalDate, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = buildJsonObject {
                    localDate.let {
                        put("date", "${it.year}-${it.monthNumber}-${it.dayOfMonth}")
                        if (has_time)
                            put("time", time.let { "${it.hoursComponent}:${it.minutesComponent}:${it.secondsComponent}" })
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::date, localDate)
            return result
        }

        /** Set the state value but just the time part. The time must be in local time for Home Assistant. */
        public suspend fun setTime(localTime: TimeOfDay, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = buildJsonObject {
                    localTime.let {
                        if (has_date)
                            put("date", date.let { "${it.year}-${it.monthNumber}-${it.dayOfMonth}" })
                        put("time", "${it.hoursComponent}:${it.minutesComponent}:${it.secondsComponent}")
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::time, localTime)
            return result
        }

        /** Set the state value. You can set just the time, date or both at once.
         * The date and time must be in local time for Home Assistant. */
        public suspend fun setDateTime(localDate: LocalDate? = null, localTime: TimeOfDay? = null) = when {
            localDate == null && localTime != null -> setTime(localTime)
            localDate != null && localTime == null -> setDate(localDate)
            localDate != null && localTime != null -> setDateTime(LocalDateTime(localDate, localTime))
            else -> throw IllegalArgumentException("Both arguments cannot be null")
        }
    }
}


/** Access the InputDateTime Domain */
public val KHomeAssistant.InputDatetime: InputDatetime
    get() = InputDatetime(this)