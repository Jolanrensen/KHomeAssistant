package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.parseUtc
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT_SUN
import nl.jolanrensen.kHomeAssistant.cast


class Sun(kHassInstance: KHomeAssistant) : Domain<Sun.Entity>, KHomeAssistant by kHassInstance {
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
    override fun Entity(name: String): Entity = Entity(this)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String = "sun"
    ) : BaseEntity<SunState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Sun(kHassInstance)
    ) {

        init {
            attributes += arrayOf(
                ::next_rising,
                ::next_setting,
                ::next_dawn,
                ::next_dusk,
                ::next_noon,
                ::next_midnight,
                ::elevation,
                ::azimuth,
                ::rising
            )
        }

        override fun stateToString(state: SunState): String = state.stateValue

        override fun stringToState(stateValue: String): SunState? = try {
            SunState.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        // ----- Attributes -----
        // read only

        /** Date and time of the next sun rising (in UTC). */
        val next_rising: DateTime
            get() = HASS_DATE_FORMAT_SUN.parseUtc(
                rawAttributes[::next_rising.name]!!.cast()!!
            )

        /** Date and time of the next sun setting (in UTC). */
        val next_setting: DateTime
            get() = HASS_DATE_FORMAT_SUN.parseUtc(
                rawAttributes[::next_setting.name]!!.cast()!!
            )

        /** Date and time of the next dawn (in UTC). */
        val next_dawn: DateTime
            get() = HASS_DATE_FORMAT_SUN.parseUtc(
                rawAttributes[::next_dawn.name]!!.cast()!!
            )

        /** Date and time of the next dusk (in UTC). */
        val next_dusk: DateTime
            get() = HASS_DATE_FORMAT_SUN.parseUtc(
                rawAttributes[::next_dusk.name]!!.cast()!!
            )

        /** Date and time of the next solar noon (in UTC). */
        val next_noon: DateTime
            get() = HASS_DATE_FORMAT_SUN.parseUtc(
                rawAttributes[::next_noon.name]!!.cast()!!
            )

        /** Date and time of the next solar midnight (in UTC). */
        val next_midnight: DateTime
            get() = HASS_DATE_FORMAT_SUN.parseUtc(
                rawAttributes[::next_midnight.name]!!.cast()!!
            )

        /** Solar elevation. This is the angle between the sun and the horizon. Negative values mean the sun is below the horizon. */
        val elevation: Float by attrsDelegate()

        /** Solar azimuth. The angle is shown clockwise from north. */
        val azimuth: Float by attrsDelegate()

        /** True if the Sun is currently rising, after solar midnight and before solar noon. */
        val rising: Boolean by attrsDelegate()

        /** True of the Sun is above the horizon. */
        val isUp: Boolean
            get() = state == SunState.ABOVE_HORIZON

        /** True of the Sun is above the horizon. */
        val isAboveHorizon: Boolean
            get() = state == SunState.ABOVE_HORIZON

        /** True of the Sun is below the horizon. */
        val isDown: Boolean
            get() = state == SunState.BELOW_HORIZON

        /** True of the Sun is below the horizon. */
        val isBelowHorizon: Boolean
            get() = state == SunState.BELOW_HORIZON

        /** Schedule something to execute each day at sunrise.
         * @see runEveryDayAtSunrise */
        suspend fun onSunrise(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            runEveryDayAtSunrise {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at sunset.
         * @see runEveryDayAtSunset */
        suspend fun onSunset(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            runEveryDayAtSunset(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at dawn.
         * @see runEveryDayAtDawn */
        suspend fun onDawn(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            runEveryDayAtDawn(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at dusk.
         * @see runEveryDayAtDusk */
        suspend fun onDusk(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            runEveryDayAtDusk(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at noon.
         * @see runEveryDayAtNoon */
        suspend fun onNoon(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            runEveryDayAtNoon(offset) {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at midnight.
         * @see runEveryDayAtMidnight */
        suspend fun onMidnight(offset: TimeSpan = TimeSpan.ZERO, callback: suspend Entity.() -> Unit): Entity {
            runEveryDayAtMidnight(offset) {
                callback(this)
            }
            return this
        }
    }

}


/** Access the Sun Domain. */
val KHomeAssistant.Sun: Sun
    get() = Sun(this)

/** As there is only one sun (duh), let's make the sun entity quickly reachable */
val KHomeAssistant.sun: Sun.Entity
    get() = Sun.Entity("")