package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.entities.getValue
import nl.jolanrensen.kHomeAssistant.helper.*
import kotlin.reflect.KProperty

/** Do not use directly! Always use Light.
 *
 * https://www.home-assistant.io/integrations/light/
 * */
object Light : Domain<Light.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "light"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Light.' from a KHomeAssistantContext instead of using Light directly.""".trimMargin()
    }


    enum class Flash(val value: String) {
        SHORT("short"), LONG("long")
    }

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_BRIGHTNESS(1),
        SUPPORT_COLOR_TEMP(2),
        SUPPORT_EFFECT(4),
        SUPPORT_FLASH(8),
        SUPPORT_COLOR(16),
        SUPPORT_TRANSITION(32),
        SUPPORT_WHITE_VALUE(128)
    }

    override fun Entity(name: String): Entity = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = Light // TODO check?
    ) {
        /** Some attributes can be set using the turn_on command. For those, we define a setter-companion to getValue. */
        @Suppress("UNCHECKED_CAST")
        operator fun <V : Any?> setValue(
            thisRef: BaseEntity<*>?,
            property: KProperty<*>,
            value: V
        ) {
            runBlocking {
                when (property.name) {
                    ::brightness.name -> turnOnWithData(brightness = value as Int)
                    ::hs_color.name -> turnOnWithData(hs_color = value as HSColor)
                    ::rgb_color.name -> turnOnWithData(rgb_color = value as RGBColor)
                    ::xy_color.name -> turnOnWithData(xy_color = value as XYColor)
                    ::white_value.name -> turnOnWithData(white_value = value as Int)
                }
                Unit
            }
        }


        // Attributes
        // read only
        val min_mireds: Int? by this
        val max_mireds: Int? by this
        val effect_list: List<String>? by this

        @OptIn(ExperimentalStdlibApi::class)
        val supported_features: Set<SupportedFeatures>
            get() = buildSet {
                val features = attributes["supported_features"]!!.int
                SupportedFeatures.values().forEach {
                    if (it.value and features == it.value)
                        add(it)
                }
            }

        // read / write

        /** Integer between 0 and 255 for how bright the light should be, where 0 means the light is off, 1 is the minimum brightness and 255 is the maximum brightness supported by the light. */
        var brightness: Int? by this

        /** An HSColor containing two floats representing the hue and saturation of the color you want the light to be. Hue is scaled 0-360, and saturation is scaled 0-100. */
        var hs_color: HSColor? by this

        /** An RGBColor containing three integers between 0 and 255 representing the RGB color you want the light to be. Note that the specified RGB value will not change the light brightness, only the color. */
        var rgb_color: RGBColor? by this

        /** An XYColor containing two floats representing the xy color you want the light to be. */
        var xy_color: XYColor? by this

        /** Integer between 0 and 255 for how bright a dedicated white LED should be. */
        var white_value: Int? by this


        // write only

        /** String with the name of one of the built-in profiles (relax, energize, concentrate, reading) or one of the custom profiles defined in light_profiles.csv in the current working directory. Light profiles define an xy color and a brightness. If a profile is given and a brightness then the profile brightness will be overwritten. */
        var profile: String
            @Deprecated("'profile' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'profile' is write only")
            set(value) {
                runBlocking { turnOnWithData(profile = value) }
            }

        /** An integer in mireds representing the color temperature you want the light to be. */
        var color_temp: Int
            @Deprecated("'color_temp' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_temp' is write only")
            set(value) {
                runBlocking { turnOnWithData(color_temp = value) }
            }

        /** Alternatively, you can specify the color temperature in Kelvin. */
        var kelvin: Int
            @Deprecated("'kelvin' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'kelvin' is write only")
            set(value) {
                runBlocking { turnOnWithData(kelvin = value) }
            }

        /** A human-readable string of a color name, such as blue or goldenrod. All CSS3 color names are supported. */
        var color_name: String
            @Deprecated("'color_name' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_name' is write only")
            set(value) {
                runBlocking { turnOnWithData(color_name = value) }
            }

        /** Alternatively, you can specify brightness in percent (a number between 0 and 100), where 0 means the light is off, 1 is the minimum brightness and 100 is the maximum brightness supported by the light. */
        var brightness_pct: Float
            @Deprecated("'brightness_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_pct' is write only")
            set(value) {
                runBlocking { turnOnWithData(brightness_pct = value) }
            }

        /** Change brightness by an amount. Should be between -255..255. */
        var brightness_step: Float
            @Deprecated("'brightness_step' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step' is write only")
            set(value) {
                runBlocking { turnOnWithData(brightness_step = value) }
            }

        /** Change brightness by a percentage. Should be between -100..100. */
        var brightness_step_pct: Float
            @Deprecated("'brightness_step_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step_pct' is write only")
            set(value) {
                runBlocking { turnOnWithData(brightness_step_pct = value) }
            }

        /** Tell light to flash, can be either value Flash.SHORT or Flash.LONG. */
        var flash: Flash
            @Deprecated("'flash' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'flash' is write only")
            set(value) {
                runBlocking { turnOnWithData(flash = value) }
            }

        /** Applies an effect such as colorloop or random. */
        var effect: String
            @Deprecated("'effect' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'effect' is write only")
            set(value) {
                runBlocking { turnOnWithData(effect = value) }
            }

        @Serializable
        inner class turnOnWithData(
            val transition: Int? = null,
            val profile: String? = null,
            val hs_color: HSColor? = null,
            val xy_color: XYColor? = null,
            val rgb_color: RGBColor? = null,
            val white_value: Int? = null,
            val color_temp: Int? = null,
            val kelvin: Int? = null,
            val color_name: String? = null,
            val brightness: Int? = null,
            val brightness_pct: Float? = null,
            val brightness_step: Float? = null,
            val brightness_step_pct: Float? = null,
            val flash: Flash? = null,
            val effect: String? = null // tODO add result callback, todo add checks for supported features
        ) {

            init {
                runBlocking {
                    // First check input
                    transition?.let {
                        if (it < 0)
                            throw IllegalArgumentException("incorrect transition $it")
                    }
                    profile?.let {
                    }
                    hs_color?.let {
                        if (it.isEmpty() || it.size > 2 || it.h !in 0f..360f || it.s !in 0f..100f)
                            throw IllegalArgumentException("incorrect hs_color $it")
                    }
                    xy_color?.let {
                        if (it.isEmpty() || it.size > 2)
                            throw IllegalArgumentException("incorrect xy_color $it")
                    }
                    rgb_color?.let {
                        if (it.isEmpty() || it.size > 3 || it.any { it !in 0..255 })
                            throw IllegalArgumentException("incorrect rgb_color $it")
                    }
                    white_value?.let {
                        if (it !in 0..255)
                            throw IllegalArgumentException("incorrect white_value $it")
                    }
                    color_temp?.let {
                        if (min_mireds == null || max_mireds == null)
                            throw IllegalArgumentException("mireds not supported for this device")
                        if (it !in min_mireds!!..max_mireds!!)
                            throw IllegalArgumentException("incorrect color_temp $it")
                    }
                    kelvin?.let {
                        if (it < 0)
                            throw IllegalArgumentException("incorrect kelvin $it")
                    }
                    color_name?.let {
                        // TODO check color name https://www.w3.org/TR/css-color-3/#svg-color
                    }
                    brightness?.let {
                        if (it !in 0..255)
                            throw IllegalArgumentException("incorrect brightness $it")
                    }
                    brightness_pct?.let {
                        if (it !in 0f..100f)
                            throw IllegalArgumentException("incorrect brightness_pct $it")
                    }
                    brightness_step?.let {
                        if (it !in -255f..255f)
                            throw IllegalArgumentException("incorrect brightness_step $it")
                    }
                    brightness_step_pct?.let {
                        if (it !in -100f..100f)
                            throw IllegalArgumentException("incorrect brightness_step_pct $it")
                    }
                    flash?.let {
                    }
                    effect?.let {
                        if (effect_list == null || it !in effect_list!!)
                            throw IllegalArgumentException("incorrect effect $it")
                    }

                    // Then serialize the json and call the service with the data
                    val data = Json(
                        JsonConfiguration.Stable.copy(encodeDefaults = false)
                    ).toJson(serializer(), this@turnOnWithData)

                    callService(
                        serviceName = "turn_on",
                        data = data.jsonObject
                    )
                }
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        suspend fun turnOff(transition: Int) =
            callService(
                serviceName = "turn_off",
                data = buildMap<String, JsonElement> {
                    transition.let {
                        if (it < 0)
                            throw IllegalArgumentException("incorrect transition $it")
                        this["transition"] = JsonPrimitive(it)
                    }
                }
            )
    }

}

/** Access the Light Domain */
typealias LightDomain = Light

val KHomeAssistantContext.Light: LightDomain
    get() = LightDomain.also { it.kHomeAssistant = kHomeAssistant }


