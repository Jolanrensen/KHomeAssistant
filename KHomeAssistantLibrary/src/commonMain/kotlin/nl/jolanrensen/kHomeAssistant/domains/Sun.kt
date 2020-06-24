package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.parseUtc
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT_SUN


class Sun(override val kHassInstance: KHomeAssistant) : Domain<Sun.Entity> {
    override val domainName = "sun"

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?) = other is Sun
    override fun hashCode(): Int = domainName.hashCode()

    enum class SunState(val stateValue: String) {
        ABOVE_HORIZON("above_horizon"),
        BELOW_HORIZON("below_horizon")
    }

    /** No need to specify a name, it's just 'sun' */
//    fun Entity(): Entity = Entity(getKHomeAssistant = kHomeAssistant)
    override fun Entity(name: String): Entity = Entity(kHassInstance)

    interface HassAttributes : BaseHassAttributes {
        /** Date and time of the next sun rising (in UTC). */
        val next_rising: String

        /** Date and time of the next sun setting (in UTC). */
        val next_setting: String

        /** Date and time of the next dawn (in UTC). */
        val next_dawn: String

        /** Date and time of the next dusk (in UTC). */
        val next_dusk: String

        /** Date and time of the next solar noon (in UTC). */
        val next_noon: String

        /** Date and time of the next solar midnight (in UTC). */
        val next_midnight: String

        /** Solar elevation. This is the angle between the sun and the horizon. Negative values mean the sun is below the horizon. */
        val elevation: Float

        /** Solar azimuth. The angle is shown clockwise from north. */
        val azimuth: Float

        /** True if the Sun is currently rising, after solar midnight and before solar noon. */
        val rising: Boolean
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String = "sun"
    ) : BaseEntity<SunState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Sun(kHassInstance)
    ), HassAttributes {

        override fun stateToString(state: SunState): String = state.stateValue

        override fun stringToState(stateValue: String): SunState? = try {
            SunState.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        // ----- Attributes -----

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        // read only

        override val next_rising: String by attrsDelegate()
        override val next_setting: String by attrsDelegate()
        override val next_dawn: String by attrsDelegate()
        override val next_dusk: String by attrsDelegate()
        override val next_noon: String by attrsDelegate()
        override val next_midnight: String by attrsDelegate()
        override val elevation: Float by attrsDelegate()
        override val azimuth: Float by attrsDelegate()
        override val rising: Boolean by attrsDelegate()

        /** Schedule something to execute each day at sunrise.
         * @see runEveryDayAtSunrise */
        suspend fun onSunrise(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            kHassInstance.runEveryDayAtSunrise {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at sunset.
         * @see runEveryDayAtSunset */
        suspend fun onSunset(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            kHassInstance.runEveryDayAtSunset(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at dawn.
         * @see runEveryDayAtDawn */
        suspend fun onDawn(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            kHassInstance.runEveryDayAtDawn(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at dusk.
         * @see runEveryDayAtDusk */
        suspend fun onDusk(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            kHassInstance.runEveryDayAtDusk(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at noon.
         * @see runEveryDayAtNoon */
        suspend fun onNoon(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            kHassInstance.runEveryDayAtNoon(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at midnight.
         * @see runEveryDayAtMidnight */
        suspend fun onMidnight(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            kHassInstance.runEveryDayAtMidnight(offset) {
                callback(this)
            }
            return this
        }
    }

}

/** Date and time of the next sun rising (in local time). */
val Sun.Entity.nextRising: DateTimeTz
    get() = HASS_DATE_FORMAT_SUN.parseUtc(next_rising).local

/** Date and time of the next sun setting (in local time). */
val Sun.Entity.nextSetting: DateTimeTz
    get() = HASS_DATE_FORMAT_SUN.parseUtc(next_setting).local

/** Date and time of the next dawn (in local time). */
val Sun.Entity.nextDawn: DateTimeTz
    get() = HASS_DATE_FORMAT_SUN.parseUtc(next_dawn).local

/** Date and time of the next dusk (in local time). */
val Sun.Entity.nextDusk: DateTimeTz
    get() = HASS_DATE_FORMAT_SUN.parseUtc(next_dusk).local

/** Date and time of the next solar noon (in local time). */
val Sun.Entity.nextNoon: DateTimeTz
    get() = HASS_DATE_FORMAT_SUN.parseUtc(next_noon).local

/** Date and time of the next solar midnight (in local time). */
val Sun.Entity.nextMidnight: DateTimeTz
    get() = HASS_DATE_FORMAT_SUN.parseUtc(next_midnight).local

/** True of the Sun is above the horizon. */
val Sun.Entity.isUp: Boolean
    get() = state == Sun.SunState.ABOVE_HORIZON

/** True of the Sun is above the horizon. */
val Sun.Entity.isAboveHorizon: Boolean
    get() = state == Sun.SunState.ABOVE_HORIZON

/** True of the Sun is below the horizon. */
val Sun.Entity.isDown: Boolean
    get() = state == Sun.SunState.BELOW_HORIZON

/** True of the Sun is below the horizon. */
val Sun.Entity.isBelowHorizon: Boolean
    get() = state == Sun.SunState.BELOW_HORIZON


/** Access the Sun Domain. */
val KHomeAssistant.Sun: Sun
    get() = Sun(this)

/** As there is only one sun (duh), let's make the sun entity quickly reachable */
val KHomeAssistant.sun: Sun.Entity
    get() = Sun.Entity("")