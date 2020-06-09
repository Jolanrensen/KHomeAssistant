package nl.jolanrensen.kHomeAssistant.domains.input

import com.soywiz.klock.*
import com.soywiz.klock.DateFormat.Companion.FORMAT_DATE
import com.soywiz.klock.TimeFormat.Companion.FORMAT_TIME
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.AttributesDelegate
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilAttributeChangedTo
import nl.jolanrensen.kHomeAssistant.helper.cast
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

/**
 * Input Datetime
 * https://www.home-assistant.io/integrations/input_datetime
 * */
class InputDatetime(override var kHomeAssistant: () -> KHomeAssistant?) : Domain<InputDatetime.Entity> {
    override val domainName = "input_datetime"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputDatetime.' from a KHomeAssistantContext instead of using InputDatetime directly.""".trimMargin()
    }

    /** Making sure InputDatetime acts as a singleton. */
    override fun equals(other: Any?) = other is InputDatetime
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_datetime configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

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

    @OptIn(ExperimentalStdlibApi::class)
    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<State>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputDatetime(kHomeAssistant)
    ) {

        init {
            attributes += arrayOf(
                ::has_time,
                ::has_date,
                ::hasDateAndTime,
                ::timestamp,
                ::editable,
                ::year,
                ::month,
                ::day,
                ::hour,
                ::minute,
                ::second,
                ::date,
                ::dateTime
            )
        }

        override fun parseStateValue(stateValue: String) = State(stateValue)

        override fun getStateValue(state: State) = state.value

        /** Some attributes can be set using the set_datetime command. For those, we define a setter-companion to getValue. */
        operator fun <V : Any?> AttributesDelegate.setValue(
            thisRef: BaseEntity<*>?,
            property: KProperty<*>,
            value: V
        ) {
            runBlocking {
                when (property.name) {
                    ::day.name -> {
                        year?.let { y ->
                            month?.let { m ->
                                setDate(localDate = Date(year = y, month = m, day = value as Int))
                            }
                        } ?: throw Exception("Can't set day, $name has no date.")
                    }
                    ::hour.name -> {
                        minute?.let { m ->
                            second?.let { s ->
                                setTime(localTime = Time(hour = value as Int, minute = m, second = s))
                            }
                        } ?: throw Exception("Can't set hour, $name has no time.")
                    }
                    ::minute.name -> {
                        hour?.let { h ->
                            second?.let { s ->
                                setTime(
                                    localTime = Time(hour = h, minute = value as Int, second = s)
                                )
                            }
                        } ?: throw Exception("Can't set minute, $name has no time.")
                    }
                    ::second.name -> {
                        hour?.let { h ->
                            minute?.let { m ->
                                setTime(localTime = Time(hour = h, minute = m, second = value as Int))
                            }
                        } ?: throw Exception("Can't set minute, $name has no time.")
                    }
                }
                Unit
            }
        }

        // Attributes
        // read only

        /** true if this entity has a time. */
        val has_time: Boolean
            get() = rawAttributes[::has_time.name]?.cast() ?: false

        /** true if this entity has a date. */
        val has_date: Boolean
            get() = rawAttributes[::has_date.name]?.cast() ?: false

        /** true if this entity has a date and a time */
        val hasDateAndTime: Boolean
            get() = has_date && has_time

        /** A UNIX timestamp representing the (UTC) date and time (in seconds) held in the input. */
        val timestamp: Long? by attrsDelegate

        val editable: Boolean? by attrsDelegate


        // read/write

        /** The year of the date, if [has_date]. Use `year!!.year` to get the year as [Int].*/
        var year: Year?
            get() = rawAttributes[::year.name]?.cast<Int>()?.let { Year(it) }
            set(value) {
                runBlocking {
                    month?.let { m ->
                        day?.let { d ->
                            setDate(localDate = Date(year = value!!, month = m, day = d))
                        }
                    } ?: throw Exception("Can't set year, $name has no date.")
                }
            }

        /** The month of the date, if [has_date]. Use `month!!.index1` to get the month as [Int] (with January == 1). */
        var month: Month?
            get() = rawAttributes[::month.name]?.cast<Int>()?.let { Month(it) }
            set(value) {
                runBlocking {
                    year?.let { y ->
                        day?.let { d ->
                            setDate(localDate = Date(year = y, month = value!!, day = d))
                        }
                    } ?: throw Exception("Can't set month, $name has no date.")
                }
            }

        /** The day (of the month) of the date, if [has_date]. */
        var day: Int? by attrsDelegate

        /** The hour of the time, if [has_time]. */
        var hour: Int? by attrsDelegate

        /** The minute of the time, if [has_time]. */
        var minute: Int? by attrsDelegate

        /** The second of the time, if [has_time]. */
        var second: Int? by attrsDelegate

        /** The date (in local time) as represented in Home Assistant, if [has_date]. */
        var date: Date?
            get() = year?.let { y -> month?.let { m -> day?.let { d -> Date(y, m, d) } } }
            set(value) {
                runBlocking {
                    setDate(localDate = value!!)
                }
            }

        /** The time (in local time) as represented in Home Assistant, if [has_time]. */
        var time: Time?
            get() = hour?.let { h -> minute?.let { m -> second?.let { s -> Time(h, m, s) } } }
            set(value) {
                runBlocking {
                    setTime(localTime = value!!)
                }
            }

        /** The date and time (in local time) as represented in Home Assistant, if [has_date] and [has_time].
         * Convert to [DateTime] using UTC time with `dateTime!!.utc`.
         * Convert to [DateTime] using local time with `dateTime!!.local`.
         * ([DateTime] is usually only for calculating with UTC time, but it has more functionality than [DateTimeTz])
         * */
        var dateTime: DateTimeTz?
            get() = date?.let { d -> time?.let { t -> DateTime(d, t).localUnadjusted } }
            set(value) {
                runBlocking {
                    setDateTime(localDateTime = value!!)
                }
            }

        /** Set the state value. To make a [DateTimeTz] (like 9 am local time), use `DateTime(someDate, Time(hour=9)).localUnadjusted`. */
        suspend fun setDateTime(localDateTime: DateTimeTz, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_datetime",
                data = buildMap<String, JsonElement> {
                    localDateTime.let {
                        if (has_date)
                            this["date"] = JsonPrimitive("${it.yearInt}-${it.month1}-${it.dayOfMonth}")

                        if (has_time)
                            this["time"] = JsonPrimitive("${it.hours}:${it.minutes}:${it.seconds}")
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
                data = buildMap<String, JsonElement> {
                    localDate.let {
                        this["date"] = JsonPrimitive("${it.year}-${it.month1}-${it.day}")
                        if (has_time)
                            this["time"] = JsonPrimitive(time!!.let { "${it.hour}:${it.minute}:${it.second}" })
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
                data = buildMap<String, JsonElement> {
                    localTime.let {
                        if (has_date)
                            this["date"] = JsonPrimitive(date!!.let { "${it.year}-${it.month1}-${it.day}" })
                        this["time"] = JsonPrimitive("${it.hour}:${it.minute}:${it.second}")
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
val KHomeAssistantContext.InputDatetime: InputDatetime
    get() = InputDatetime(kHomeAssistant)