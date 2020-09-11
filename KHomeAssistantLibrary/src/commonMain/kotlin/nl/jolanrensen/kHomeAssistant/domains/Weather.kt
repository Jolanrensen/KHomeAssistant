package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import kotlinx.serialization.json.*
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT_SUN

class Weather(override val kHassInstance: KHomeAssistant) : Domain<Weather.Entity> {
    override val domainName: String = "weather"

    /** Making sure Weather acts as a singleton. */
    override fun equals(other: Any?) = other is Weather
    override fun hashCode(): Int = domainName.hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    data class Forecast(
        /** The date time of the forecast. */
        val dateTime: DateTimeTz,

        /** The higher temperature in °C or °F. */
        val temperatureHigher: Float,

        /** The weather condition at this point. */
        val condition: State,

        /** The lower daily Temperature in °C or °F. */
        val temperatureLower: Float?,

        /** The precipitation amount in mm or inch. */
        val precipitation: Float?,

        /** The probability of precipitation in %. */
        val precipitationProbability: Int?
    ) {
        override fun toString() = """Forecast {
            |   dateTime = $dateTime,
            |   temperatureHigher = $temperatureHigher,
            |   condition = $condition,
            |   temperatureLower = $temperatureLower,
            |   precipitation = $precipitation,
            |   precipitationProbability = $precipitationProbability
            |}
        """.trimMargin()
    }

    enum class State(val value: String?) {
        CLEAR_NIGHT("clear-night"),
        CLOUDY("cloudy"),
        EXCEPTIONAL("exceptional"),
        FOG("fog"),
        HAIL("hail"),
        LIGHTNING("lightning"),
        LIGHTNING_RAINY("lightning-rainy"),
        PARTLY_CLOUDY("partlycloudy"),
        POURING_RAIN("pouring"),
        RAINY("rainy"),
        SNOWY("snowy"),
        SNOWY_RAINY("snowy-rainy"),
        SUNNY("sunny"),
        WINDY("windy"),
        WINDY_VARIANT("windy-variant"),
        UNKNOWN(null)
    }

    interface HassAttributes : BaseHassAttributes {
        // Read only

        /** The current temperature in °C or °F. */
        val temperature: Float

        /** The current air pressure in hPa or inHg. */
        val pressure: Float

        /** The current humidity in %. */
        val humidity: Float

        /** The current visibility in km or mi. */
        val visibility: Float

        /** The current wind speed in km/h or mi/h. */
        val wind_speed: Float

        /** The current wind bearing, 1-3 letters or degree. */
        val wind_bearing: String

        /** Daily or Hourly forecast data. @see [forecast_] */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("forecast_"))
        val forecast: List<JsonObject>

        /** The branding text required by the API provider. */
        val attribution: String

        // Helper

        /** Daily or Hourly forecast data. */
        val forecast_: List<Forecast>
            get() = forecast.map {
                Forecast(
                    dateTime = HASS_DATE_FORMAT_SUN.parse(it["datetime"]!!.jsonPrimitive.content),
                    temperatureHigher = it["temperature"]!!.jsonPrimitive.float,
                    condition = State.values().find { value ->
                        it["condition"]?.jsonPrimitive?.contentOrNull == value.value
                    } ?: State.UNKNOWN,
                    temperatureLower = it["templow"]?.jsonPrimitive?.floatOrNull,
                    precipitation = it["precipitation"]?.jsonPrimitive?.floatOrNull,
                    precipitationProbability = it["precipitation_probability"]?.jsonPrimitive?.intOrNull

                )
            }
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<State, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Weather(kHassInstance)
    ), HassAttributes {
        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()
        override val additionalToStringAttributes: Array<Attribute<*>> = super.additionalToStringAttributes + ::forecast_

        override fun stateToString(state: State): String? = state.value
        override fun stringToState(stateValue: String): State? = State.values().find { it.value == stateValue }

        // Attributes
        override val temperature: Float by attrsDelegate()
        override val pressure: Float by attrsDelegate()
        override val humidity: Float by attrsDelegate()
        override val visibility: Float by attrsDelegate()
        override val wind_speed: Float by attrsDelegate()
        override val wind_bearing: String by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("forecast_"))
        override val forecast: List<JsonObject> by attrsDelegate()
        override val attribution: String by attrsDelegate()
    }
}


/** Access the Weather Domain */
val KHomeAssistant.Weather: Weather
    get() = Weather(this)