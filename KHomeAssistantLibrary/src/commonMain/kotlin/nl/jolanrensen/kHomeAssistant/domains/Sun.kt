package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.parseUtc
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.entities.*
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

        /** Date and time of the next sun rising (in UTC). @see [nextRising]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextRising"))
        val next_rising: String

        /** Date and time of the next sun setting (in UTC). @see [nextSetting]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextSetting"))
        val next_setting: String

        /** Date and time of the next dawn (in UTC). @see [nextDawn]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextDawn"))
        val next_dawn: String

        /** Date and time of the next dusk (in UTC). @see [nextDusk]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextDusk"))
        val next_dusk: String

        /** Date and time of the next solar noon (in UTC). @see [nextNoon]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextNoon"))
        val next_noon: String

        /** Date and time of the next solar midnight (in UTC). @see [nextMidnight]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextMidnight"))
        val next_midnight: String

        /** Solar elevation. This is the angle between the sun and the horizon. Negative values mean the sun is below the horizon. */
        val elevation: Float

        /** Solar azimuth. The angle is shown clockwise from north. */
        val azimuth: Float

        /** True if the Sun is currently rising, after solar midnight and before solar noon. */
        val rising: Boolean

        // Helper getter/setters
        
        /** Date and time of the next sun rising (in local time). */
        val nextRising: DateTimeTz
            get() = HASS_DATE_FORMAT_SUN.parseUtc(next_rising).local

        /** Date and time of the next sun setting (in local time). */
        val nextSetting: DateTimeTz
            get() = HASS_DATE_FORMAT_SUN.parseUtc(next_setting).local

        /** Date and time of the next dawn (in local time). */
        val nextDawn: DateTimeTz
            get() = HASS_DATE_FORMAT_SUN.parseUtc(next_dawn).local

        /** Date and time of the next dusk (in local time). */
        val nextDusk: DateTimeTz
            get() = HASS_DATE_FORMAT_SUN.parseUtc(next_dusk).local

        /** Date and time of the next solar noon (in local time). */
        val nextNoon: DateTimeTz
            get() = HASS_DATE_FORMAT_SUN.parseUtc(next_noon).local

        /** Date and time of the next solar midnight (in local time). */
        val nextMidnight: DateTimeTz
            get() = HASS_DATE_FORMAT_SUN.parseUtc(next_midnight).local
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String = "sun"
    ) : BaseEntity<SunState, HassAttributes>(
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
        override val additionalToStringAttributes: Array<Attribute<*>> = super.additionalToStringAttributes +
                getHassAttributesHelpers<HassAttributes>()

        // read only
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextRising"))
        override val next_rising: String by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextSetting"))
        override val next_setting: String by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextDawn"))
        override val next_dawn: String by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextDusk"))
        override val next_dusk: String by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextNoon"))
        override val next_noon: String by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("nextMidnight"))
        override val next_midnight: String by attrsDelegate()
        override val elevation: Float by attrsDelegate()
        override val azimuth: Float by attrsDelegate()
        override val rising: Boolean by attrsDelegate()

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


/** Access the Sun Domain. */
val KHomeAssistant.Sun: Sun
    get() = Sun(this)

/** As there is only one sun (duh), let's make the sun entity quickly reachable */
val KHomeAssistant.sun: Sun.Entity
    get() = Sun.Entity("")