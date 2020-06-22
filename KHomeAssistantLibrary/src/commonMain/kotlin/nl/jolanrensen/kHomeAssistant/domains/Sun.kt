package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.DateTime
import com.soywiz.klock.parseUtc
import nl.jolanrensen.kHomeAssistant.*
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT_SUN
import nl.jolanrensen.kHomeAssistant.helper.cast


class Sun(override var getKHomeAssistant: () -> KHomeAssistant?) : Domain<Sun.Entity> {
    override val domainName = "sun"

    override fun checkContext() = require(getKHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Sun.' from a KHomeAssistantContext instead of using Sun directly.""".trimMargin()
    }

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?) = other is Sun
    override fun hashCode(): Int = domainName.hashCode()

    enum class SunState(val stateValue: String) {
        ABOVE_HORIZON("above_horizon"),
        BELOW_HORIZON("below_horizon")
    }

    /** No need to specify a name, it's just 'sun' */
//    fun Entity(): Entity = Entity(getKHomeAssistant = kHomeAssistant)
    override fun Entity(name: String): Entity = Entity(getKHomeAssistant)

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String = "sun"
    ) : BaseEntity<SunState>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = Sun(getKHomeAssistant)
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

        override fun getStateValue(state: SunState): String = state.stateValue

        override fun parseStateValue(stateValue: String): SunState? = try {
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
        suspend fun onSunrise(callback: suspend Entity.() -> Unit): Entity {
            getKHomeAssistant()!!.runEveryDayAtSunrise {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at sunset.
         * @see runEveryDayAtSunset */
        suspend fun onSunSet(callback: suspend Entity.() -> Unit): Entity {
            getKHomeAssistant()!!.runEveryDayAtSunset {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at dawn.
         * @see runEveryDayAtDawn */
        suspend fun onDawn(callback: suspend Entity.() -> Unit): Entity {
            getKHomeAssistant()!!.runEveryDayAtDawn {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at dusk.
         * @see runEveryDayAtDusk */
        suspend fun onDusk(callback: suspend Entity.() -> Unit): Entity {
            getKHomeAssistant()!!.runEveryDayAtDusk {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at noon.
         * @see runEveryDayAtNoon */
        suspend fun onNoon(callback: suspend Entity.() -> Unit): Entity {
            getKHomeAssistant()!!.runEveryDayAtNoon {
                callback(this)
            }
            return this
        }

        /** Schedule something to execute each day at midnight.
         * @see runEveryDayAtMidnight */
        suspend fun onMidnight(callback: suspend Entity.() -> Unit): Entity {
            getKHomeAssistant()!!.runEveryDayAtMidnight {
                callback(this)
            }
            return this
        }
    }

}


/** Access the Sun Domain. */
val HasContext.Sun: Sun
    get() = Sun(getKHomeAssistant)

/** As there is only one sun (duh), let's make the sun entity quickly reachable */
val HasContext.sun: Sun.Entity
    get() = Sun.Entity("")