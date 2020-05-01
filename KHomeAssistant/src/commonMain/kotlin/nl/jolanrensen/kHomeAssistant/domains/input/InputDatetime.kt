package nl.jolanrensen.kHomeAssistant.domains.input

import com.soywiz.klock.*
import com.soywiz.klock.DateFormat.Companion.FORMAT_DATE
import com.soywiz.klock.TimeFormat.Companion.FORMAT_TIME
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.helper.cast

/**
 * Input Datetime
 * https://www.home-assistant.io/integrations/input_datetime
 * */
object InputDatetime : Domain<InputDatetime.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "input_datetime"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputDatetime.' from a KHomeAssistantContext instead of using InputDatetime directly.""".trimMargin()
    }

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

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<State>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputDatetime
    ) {

        init {
            TODO()
        }

        override fun parseStateValue(stateValue: String) = State(stateValue)

        override fun getStateValue(state: State) = state.value

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


        // read/write TODO

        /** The year of the date, if [has_date]. Use `year!!.year` to get the year as [Int].*/
        val year: Year?
            get() = rawAttributes[::year.name]?.cast<Int>()?.let { Year(it) }

        /** The month of the date, if [has_date]. Use `month!!.index1` to get the month as [Int]. */
        val month: Month?
            get() = rawAttributes[::month.name]?.cast<Int>()?.let { Month(it) }

        /** The day (of the month) of the date, if [has_date]. */
        val day: Int? by attrsDelegate

        /** The hour of the time, if [has_time]. */
        val hour: Int? by attrsDelegate

        /** The minute of the time, if [has_time]. */
        val minute: Int? by attrsDelegate

        /** The second of the time, if [has_time]. */
        val second: Int? by attrsDelegate


        // TODO date, time, datetime etc

        // TODO setvalue, read/write
    }


}