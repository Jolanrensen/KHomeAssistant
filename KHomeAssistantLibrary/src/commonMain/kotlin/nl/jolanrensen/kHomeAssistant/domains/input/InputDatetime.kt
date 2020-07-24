package nl.jolanrensen.kHomeAssistant.domains.input

import com.soywiz.klock.*
import com.soywiz.klock.DateFormat.Companion.FORMAT_DATE
import com.soywiz.klock.TimeFormat.Companion.FORMAT_TIME
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.runAt
import nl.jolanrensen.kHomeAssistant.runEveryDayAt

/**
 * Input Datetime
 * https://www.home-assistant.io/integrations/input_datetime
 * */
class InputDatetime(override val kHassInstance: KHomeAssistant) : Domain<InputDatetime.Entity> {
    override val domainName = "input_datetime"

    /** Making sure InputDatetime acts as a singleton. */
    override fun equals(other: Any?) = other is InputDatetime
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_datetime configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String) = Entity(kHassInstance = kHassInstance, name = name)

    class State(val value: String) {
        /** Only use if `has_time == true && has_date == false`. */
        val asTime: Time
            get() = FORMAT_TIME.parseTime(value)

        /** Only use if `has_time == false && has_date == true`. */
        val asDate: Date
            get() = FORMAT_DATE.parseDate(value)

        /** Only use if `has_time == true && has_date == true`. */
        val asDateTime: DateTimeTz
            get() = value.split(" ").let {
                DateTime(
                    date = FORMAT_DATE.parseDate(it.first()),
                    time = FORMAT_TIME.parseTime(it.last())
                ).localUnadjusted
            }
    }

    interface HassAttributes : BaseHassAttributes {
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

        /** The year of the date. @see [year_]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("year_"))
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

        /** The year of the date, if [has_date]. Use `year_.year` to get the year as [Int].*/
        var year_: Year
            get() = Year(year)
            set(value) {
                year = value.year
            }

        /** The month of the date, if [has_date]. Use `month_.index1` to get the month as [Int] (with January == 1). */
        var month_: Month
            get() = Month(month)
            set(value) {
                month = value.index1
            }

        /** The date (in local time) as represented in Home Assistant, if [has_date]. */
        var date: Date
            get() = Date(year, month, day)
            set(value) {
                year = value.year
                month = value.month1
                day = value.day
            }

        /** The time (in local time) as represented in Home Assistant, if [has_time]. */
        var time: Time
            get() = Time(hour, minute, second)
            set(value) {
                hour = value.hour
                minute = value.minute
                second = value.second
            }

        /** The date and time (in local time) as represented in Home Assistant, if [has_date] and [has_time].
         * Convert to [DateTime] using UTC time with `dateTime!!.utc`.
         * Convert to [DateTime] using local time with `dateTime!!.local`.
         * ([DateTime] is usually only for calculating with UTC time, but it has more functionality than [DateTimeTz])
         * */
        var dateTime: DateTimeTz
            get() = DateTime(date, time).localUnadjusted
            set(value) {
                date = value.local.date
                time = value.local.time
            }
    }

    @OptIn(ExperimentalStdlibApi::class)
    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<State, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputDatetime(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        override fun stringToState(stateValue: String) = State(stateValue)
        override fun stateToString(state: State) = state.value

        /** Some attributes can be set using the set_datetime command. For those, we define a setter-companion to getValue. */
        override suspend fun <V : Any?> setValue(
            propertyName: String,
            value: V
        ) {
            when (propertyName) {
                ::year.name -> try {
                    setDate(localDate = Date(year = value as Int, month = month, day = day))
                } catch (e: Exception) {
                    throw Exception("Can't set year, $name has no date.", e)
                }
                ::month.name -> try {
                    setDate(localDate = Date(year = year, month = value as Int, day = day))
                } catch (e: Exception) {
                    throw Exception("Can't set month, $name has no date.", e)
                }
                ::day.name -> try {
                    setDate(localDate = Date(year = year, month = month, day = value as Int))
                } catch (e: Exception) {
                    throw Exception("Can't set day, $name has no date.", e)
                }
                ::hour.name -> try {
                    setTime(localTime = Time(hour = value as Int, minute = minute, second = second))
                } catch (e: Exception) {
                    throw Exception("Can't set hour, $name has no time.", e)
                }
                ::minute.name -> try {
                    setTime(localTime = Time(hour = hour, minute = value as Int, second = second))
                } catch (e: Exception) {
                    throw Exception("Can't set minute, $name has no time.", e)
                }
                ::second.name -> try {
                    setTime(localTime = Time(hour = hour, minute = minute, second = value as Int))
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
        override var date: Date
            get() = super.date
            set(value) {
                runBlocking {
                    setDate(localDate = value)
                }
            }
        override var time: Time
            get() = super.time
            set(value) {
                runBlocking {
                    setTime(localTime = value)
                }
            }
        override var dateTime: DateTimeTz
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
        suspend fun runAtDateTime(callback: suspend Entity.() -> Unit): Entity {
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
        suspend fun runAtDate(localTime: Time = Time(0), callback: suspend Entity.() -> Unit): Entity {
            if (!has_date) throw IllegalArgumentException("entity $name does not have a date.")
            kHassInstance.runAt(
                getNextLocalExecutionTime = { DateTime(date, localTime).localUnadjusted },
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
        suspend fun runEveryDayAtTime(callback: suspend Entity.() -> Unit): Entity {
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
        suspend fun setDateTime(localDateTime: DateTimeTz, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = json {
                    localDateTime.let {
                        if (has_date) {
                            "date" to ("${it.yearInt}-${it.month1}-${it.dayOfMonth}")
                        }

                        if (has_time)
                            "time" to "${it.hours}:${it.minutes}:${it.seconds}"
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::dateTime, localDateTime)
            return result
        }


        /** Set the state value, but just the date part. The date must be in local time for Home Assistant. */
        suspend fun setDate(localDate: Date, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = json {
                    localDate.let {
                        "date" to "${it.year}-${it.month1}-${it.day}"
                        if (has_time)
                            "time" to time.let { "${it.hour}:${it.minute}:${it.second}" }
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::date, localDate)
            return result
        }

        /** Set the state value but just the time part. The time must be in local time for Home Assistant. */
        suspend fun setTime(localTime: Time, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = json {
                    localTime.let {
                        if (has_date)
                            "date" to date.let { "${it.year}-${it.month1}-${it.day}" }
                        "time" to "${it.hour}:${it.minute}:${it.second}"
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::time, localTime)
            return result
        }

        /** Set the state value. You can set just the time, date or both at once.
         * The date and time must be in local time for Home Assistant. */
        suspend fun setDateTime(localDate: Date? = null, localTime: Time? = null) = when {
            localDate == null && localTime != null -> setTime(localTime)
            localDate != null && localTime == null -> setDate(localDate)
            localDate != null && localTime != null -> setDateTime(DateTime(localDate, localTime).localUnadjusted)
            else -> throw IllegalArgumentException("Both arguments cannot be null")
        }
    }
}


/** Access the InputDateTime Domain */
val KHomeAssistant.InputDatetime: InputDatetime
    get() = InputDatetime(this)